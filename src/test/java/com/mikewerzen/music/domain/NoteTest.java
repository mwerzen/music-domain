package com.mikewerzen.music.domain;

import org.junit.Test;

import java.util.stream.Collectors;

public class NoteTest
{

	public static void main(String[] args)
	{
		System.out.println(new NoteTest().buildTable());
	}

	@Test
	public void test()
	{
		Note root = new Note("C");
		Chord chord = new Chord("C/F#");

		System.out.println(chord);

		System.out.println(chord.matchesNotesAndInversion(new Chord("Cadd9").getNotes()));
		System.out.println(chord.getInversion(1));
		System.out.println(chord.getInversion(3));

		System.out.println(Scale.CHROMATIC.getNotes(root));

	}

	private String buildTable()
	{
		StringBuilder builder = new StringBuilder();

		String note = "C";
		Note root = new Note(note);

		builder.append("<table><thead><tr>");
		builder.append(generateTag("th", "Name"));
		builder.append(generateTag("th", "Abbreviation"));
		builder.append(generateTag("th", "Formula"));
		builder.append("</tr></thead><tbody>");
		for (ChordStructure structure : ChordStructure.values())
		{
			builder.append("<tr>");
			builder.append(generateTag("td", structure.getName()));
			String abbreviations = structure.getSuffixes().stream().map(s -> note + s + " ").collect(Collectors.joining()).trim().replace(" ", ", ");
			builder.append(generateTag("td", abbreviations));
			String formula = structure.getNotesWithIntervalNames(root).stream().map(n -> n.getName() + " ").collect(
					Collectors.joining()).trim().replace(" ", " - ");
			builder.append(generateTag("td", formula));
			builder.append("</tr>");
		}
		builder.append("</tbody></table>");
		return builder.toString();
	}

	private String generateTag(String type, String text)
	{
		return "<" + type + ">" + text + "</" + type + ">";
	}
}

