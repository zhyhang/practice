/**
 * 
 */
package com.zyh.java8.feature.files;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author zhyhang
 *
 */
public class WalkFileTree {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// see FileVisitor examples
		// replace Jpa @JoinColumn annotation
		Pattern jcPattern = Pattern.compile("^(\\s*@JoinColumn\\s*\\(\\s*name\\s*=\\s*\")(\\w+)(\")");
		replaceJcName(jcPattern, "d:/temp");
	}

	private static void replaceJcName(Pattern jcPattern, String srcDir) throws Exception {
		Files.walkFileTree(Paths.get(srcDir), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
				new FileVisitor<Path>() {

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (file.toString().endsWith(".java")) {
							Path tmpFile = file.resolveSibling(file.getFileName().toString() + ".tmp");
							PrintWriter writer = new PrintWriter(
									Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8));
							Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8);
							AtomicBoolean found = new AtomicBoolean(false);
							lines.forEach(line -> {
								Matcher matcher = jcPattern.matcher(line);
								if (matcher.find()) {
									String jcName = matcher.group(2);
									String newJcName = toLowerUpperForm(jcName);
									if (!jcName.equals(newJcName)) {
										found.set(true);
										writer.println(matcher.replaceFirst("$1" + newJcName + "$3"));
									} else {
										writer.println(line);
									}
								} else {
									writer.println(line);
								}
							});
							lines.close();
							writer.close();
							if (found.get()) {
								Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING);
								System.out.println("1-->" + file.toString());
							} else {
								Files.delete(tmpFile);
								System.out.println("0-->" + file.toString());
							}
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						return FileVisitResult.TERMINATE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}
				});

	}

	private static String toLowerUpperForm(String oldName) {
		// a_bc_de->aBcDe
		StringBuffer buf = new StringBuffer(oldName.length());
		int i = 0;
		for (; i < oldName.length() - 1; i++) {
			if (oldName.charAt(i) == '_' && oldName.charAt(i + 1) != '_') {
				buf.append(Character.toUpperCase(oldName.charAt(++i)));
			} else {
				buf.append(oldName.charAt(i));
			}
		}
		if (i == oldName.length() - 1) {
			buf.append(oldName.charAt(i));
		}
		return buf.toString();
	}

}
