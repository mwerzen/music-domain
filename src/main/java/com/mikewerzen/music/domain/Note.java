package com.mikewerzen.music.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Note implements Comparable<Note>
{
	private String name;
	private Letter letter;
	private int octave;
	private int semitonesAboveC;

	public Note(String name)
	{
		this.name = name;
		this.letter = Letter.valueOf(name.substring(0, 1));
		int accidentals = name.chars().map(c -> c == '#' ? 1 : c == 'b' ? -1 : 0).sum();
		this.semitonesAboveC = letter.getSemitonesFromC() + accidentals;
		this.octave = getOctaveOrDefault(name);;
	}

	public Note(String name, int octave)
	{
		this(name);
		this.octave = octave;
	}

	public Note(String name, Letter letter, int octave, int semitonesAboveC)
	{
		this.name = name;
		this.letter = letter;
		this.octave = octave;
		this.semitonesAboveC = semitonesAboveC;
	}

	public Note(int semitonesAboveLowestC)
	{
		octave = Math.floorDiv(semitonesAboveLowestC, 12) - 1;
		semitonesAboveC = semitonesAboveLowestC % 12;

		letter = Arrays.stream(Letter.values())
				.filter(letter -> letter.getSemitonesFromC() <= semitonesAboveC)
				.reduce((f, s) -> s)
				.orElseThrow(() -> new RuntimeException("Could Not Find Letter"));

		name = letter.name() + getAccidentals(letter, semitonesAboveC);
		getEnharmonicNote().ifPresent(enharmonic -> name = name + "/" + enharmonic.getName());
	}

	private static int getOctaveOrDefault(String name)
	{
		String octave = name.substring(name.length() - 1);
		int oct = 4;
		try { oct = Integer.parseInt(octave); } catch (Exception e) { oct = 4; }
		return oct;
	}


	public Note addInterval(Interval interval)
	{
		int noteOctave = octave;
		int noteLetterIndex = letter.getIndex() + interval.getNumber().getIndex();

		//B#4 = C5 fix, but this is just a patch, whole method needs to avoid letter arithmetic
		if (letter.equals(Letter.B) && semitonesAboveC == 0)
			noteOctave--;

		if(noteLetterIndex >= Constants.LETTERS_LENGTH)
		{
			noteOctave += Math.floor(noteLetterIndex / Constants.LETTERS_LENGTH);
			noteLetterIndex = noteLetterIndex % Constants.LETTERS_LENGTH;
		}

		Letter noteLetter = Letter.values()[noteLetterIndex];

		int noteSemitones = semitonesAboveC + interval.getSemitones();
		int letterSemitones = noteLetter.getSemitonesFromC() + ((noteOctave - octave) * Constants.SCALE_LENGTH);
		int accidentalsNeeded = noteSemitones - letterSemitones;

		String name = noteLetter.name() + getAccidentals(accidentalsNeeded);

		noteSemitones = noteSemitones - ((noteOctave - octave) * Constants.SCALE_LENGTH);

		while(noteSemitones >= Constants.SCALE_LENGTH)
		{
			noteSemitones -= Constants.SCALE_LENGTH;
			noteOctave++;
		}

		return new Note(name, noteLetter, noteOctave, noteSemitones);


	}

	public Note subtractInterval(Interval interval)
	{
		if(interval.getNumber().isCompound())
		{
			Note newNote = addInterval(interval.invert().invert());
			newNote = newNote.setOctave(newNote.octave - 2);
			return newNote;
		}

		Note newNote = addInterval(interval.invert());
		newNote = newNote.setOctave(newNote.octave - 1);
		return newNote;
	}

	private static String getAccidentals(Letter letter,  int desiredSemitonesAboveC)
	{
		return getAccidentals(desiredSemitonesAboveC - letter.getSemitonesFromC());
	}

	private static String getAccidentals(int accidentalsNeeded)
	{
		StringBuilder stringBuilder = new StringBuilder();
		while(accidentalsNeeded > 0)
		{
			stringBuilder.append(Constants.SHARP);
			accidentalsNeeded--;
		}

		while(accidentalsNeeded < 0)
		{
			stringBuilder.append(Constants.FLAT);
			accidentalsNeeded++;
		}

		return stringBuilder.toString();
	}

	public Optional<Note> getEnharmonicNote()
	{
		int enharmonicLetterIndex = getLetter().getIndex();

		if(name.contains("#"))
		{
			enharmonicLetterIndex++;
		}
		else if (name.contains("b"))
		{
			enharmonicLetterIndex--;
		}
		else
		{
			return Optional.empty();
		}

		if(enharmonicLetterIndex >= Constants.LETTERS_LENGTH)
		{
			enharmonicLetterIndex = enharmonicLetterIndex % Constants.LETTERS_LENGTH;
		}

		Letter enharmonicLetter = Letter.values()[enharmonicLetterIndex];

		String name = enharmonicLetter.name() + getAccidentals(enharmonicLetter, semitonesAboveC);

		return Optional.of(new Note(name, enharmonicLetter, octave, semitonesAboveC ));
	}

	public String getNameWithEnharmonics()
	{
		StringBuilder nameBuilder = new StringBuilder(getName());
		getEnharmonicNote().ifPresent(note -> nameBuilder.append("/").append(note.getName()));
		return nameBuilder.toString();
	}

	public int getSemitonesFromLowestC() {
		return getSemitonesAboveC() + ((octave + 1) * 12);
	}

	public Note setOctave(int octave) {
		return new Note(name, letter, octave, semitonesAboveC);
	}

	public String getName()
	{
		return name;
	}

	public Letter getLetter()
	{
		return letter;
	}

	public int getOctave()
	{
		return octave;
	}

	public int getSemitonesAboveC()
	{
		return semitonesAboveC;
	}

	public Note withName(String newName)
	{
		return new Note(newName, letter, octave, semitonesAboveC);
	}

	@Override public String toString()
	{
		return "Note{" +
				"name='" + name + '\'' +
				", letter=" + letter +
				", octave=" + octave +
				", semitonesAboveC=" + semitonesAboveC +
				'}';
	}

	@Override public int compareTo(Note o)
	{
		int mySemitones = getSemitonesFromLowestC();
		int otherSemitones = o.getSemitonesFromLowestC();

		return Integer.compare(mySemitones, otherSemitones);
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		Note note = (Note) o;
		return getSemitonesFromLowestC() == note.getSemitonesFromLowestC();
	}

	@Override public int hashCode()
	{
		return Objects.hash(getSemitonesFromLowestC());
	}
}
