/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * Author: Anas H. Sulaiman 
 */
public class IOUtils {
	private static IOUtils ourInstance = new IOUtils();

	private IOUtils() {
	}

	public static IOUtils getInstance() {
		return ourInstance;
	}

	/**
	 * Get the name of the file without extension.
	 *
	 * @param filePath a string represents a path to a file
	 * @return a string represents file name without extension; an empty string if the filePath doesn't have a file or if it's empty.
	 */
	public String getFileName(String filePath) {
		Path p = Paths.get(filePath).getFileName();
		if (p == null) return "";
		String nameWithExt = p.toString();
		int dotIndex = nameWithExt.lastIndexOf('.');
		if (dotIndex < 0) return nameWithExt;
		return nameWithExt.substring(0, dotIndex);
	}

	/**
	 * Get the name of the file with extension.
	 *
	 * @param filePath a string represents a path to a file
	 * @return a string represents file name with extension; an empty string if the filePath doesn't have file or if it's empty
	 */
	public String getFileNameWithExt(String filePath) {
		Path p = Paths.get(filePath).getFileName();
		if (p == null) return "";
		return Paths.get(filePath).getFileName().toString();
	}

	/**
	 * Get the extension of the file.
	 *
	 * @param filePath a string represents a path to a file
	 * @return a string represents file extension without the dot; an empty string if the filePath doesn't have a file or if it's empty or the file doesn't have an extension.
	 */
	public String getFileExt(String filePath) {
		Path p = Paths.get(filePath).getFileName();
		if (p == null) return "";
		String nameWithExt = p.toString();
		int dotIndex = nameWithExt.lastIndexOf('.');
		if (dotIndex < 0) return "";
		return nameWithExt.substring(dotIndex + 1, nameWithExt.length());
	}

	/**
	 * Get names of the parent directories in the specified file path. For example: "test1/test2/test3/test4.ext" will return {"test1", "test2", "test3"}.`
	 *
	 * @param filePath a string represents a path to a file
	 * @return a list of strings that represent parent directories names; an empty list if the filePath is empty or doesn't have parents.
	 */
	public List<String> getFileParentNames(String filePath) {
		Path p = Paths.get(filePath);
		List<String> names = new ArrayList<>();
		if (p == null) return names;

		int limit = p.getNameCount() - 1;
		for (int i = 0; i < limit; ++i) {
			names.add(p.getName(i).toString());
		}

		return names;
	}

	/**
	 * Creates a hidden directory in Unix & Windows.
	 * <p>Hidden is defined as: File name starts with a period (.) and the file has {@code dos:hidden} attribute.</p>
	 * <p>If a file already exists, an {@link java.io.IOException} exception is thrown.</p>
	 * <p>Any necessary parent directories will be created without being hidden.</p>
	 *
	 * @param path desired directory path
	 * @return a string that represents the created hidden directory path
	 * @throws java.io.IOException if a file with same path already exists
	 * @throws java.nio.file.InvalidPathException
	 *                             if the path string cannot be converted to a Path
	 */
	public String createHiddenDir(String path) throws IOException {
		Path p = Paths.get(path).toAbsolutePath();
		if (Files.exists(p, NOFOLLOW_LINKS)) throw new IOException("file already exists");
		if (!p.getFileName().toString().startsWith(".")) {
			Path p2 = p.getParent().resolve("." + p.getFileName());
			if (Files.exists(p2, NOFOLLOW_LINKS)) throw new IOException("file already exists");
			p = p2;
		}
		Files.createDirectories(p);
		Files.setAttribute(p, "dos:hidden", true, NOFOLLOW_LINKS);
		return p.toString();
	}

	/**
	 * Converts the specified string to an absolute and normalized path.
	 *
	 * @param path a string represents the specified path
	 * @return an absolute normalized version of the specified path
	 * @see java.nio.file.Path#toAbsolutePath()
	 * @see java.nio.file.Path#normalize()
	 */
	public String getAbsNormPath(String path) {
		return Paths.get(path).toAbsolutePath().normalize().toString();
	}

	/**
	 * <p>
	 * Extracts files from a given zip file into a given destinations.
	 * </p>
	 * If the destination path is a relative one, it'll be resolved to the given
	 * zip file path.
	 *
	 * @param zipPath         the zip file path to extract from
	 * @param toPath          the directory to extract to
	 * @param replaceExisting true to replace existing files; false to skip (attributes may
	 *                        be modified such as last modified time)
	 * @return the destination directory
	 * @throws IOException              if I/O error happens
	 * @throws IllegalArgumentException if zip file is not a file or destination path is not a
	 *                                  directory
	 * @throws java.nio.file.InvalidPathException
	 *                                  if the given path is not valid
	 */
	public String unzip(String zipPath, String toPath, boolean replaceExisting) throws IOException {

		final Path source = Paths.get(zipPath);
		if (!Files.isRegularFile(source, NOFOLLOW_LINKS)) throw new IllegalArgumentException("invalid file");

		Path target;
		if (toPath.isEmpty()) {
			target = source.getParent();
		} else {
			target = Paths.get(toPath);
			if (!target.isAbsolute()) {
				// since the two paths come from the same provider,
				// it's ok to use .resolve(Path)
				target = source.getParent().resolve(target);
			}
		}

		Files.createDirectories(target);

		URI uri = URI.create("jar:" + source.toUri().toString());
		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		env.put("encoding", "UTF-8");
		FileSystem zipFs = FileSystems.newFileSystem(uri, env);

		CopyOption[] opt = replaceExisting ? new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};

		Path zipRoot = zipFs.getPath("/");
		copy(zipRoot, target, opt);
		zipFs.close();
		return target.toString();
	}

	/**
	 * Zips a list of files into a zip file. Only regular files are zipped. If
	 * directories were provided, they are ignored.
	 *
	 * @param zipPath         the zip file path. will be created if it doesn't exist.
	 * @param paths           files to be zipped
	 * @param replaceExisting true to replace existing files; false to skip (attributes may
	 *                        be modified such as last modified time)
	 * @return the zip file path string
	 * @throws IOException          if I/O error happens
	 * @throws InvalidPathException if the given path is not valid
	 */
	public String zipFiles(boolean replaceExisting, String zipPath, String... paths) throws IOException {
		Path target = Paths.get(zipPath);

		URI uri = URI.create("jar:" + target.toUri().toString());
		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		env.put("encoding", "UTF-8");
		FileSystem zipFs = FileSystems.newFileSystem(uri, env);

		CopyOption[] opt = replaceExisting ? new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};

		for (String path : paths) {
			Path from = Paths.get(path);
			Path to = zipFs.getPath("/" + from.getFileName());
			Files.copy(from, to, opt);
		}
		zipFs.close();
		return zipPath;
	}

	/**
	 * Copies a file from the specified source to the specified destination using the specified options. If the specified source is a directory, recursively copy all sub-files and sub-directories.
	 * If the specified source is from a different provider than the specified target, it should be OK. However, this method has only been tested with the default provider and the {@link com.sun.nio.zipfs.ZipFileSystemProvider}.
	 * If a file cannot be copies for some reason, it's ignored (along with its sub-tree if it's a directory).
	 *
	 * @param source  the source file
	 * @param target  the target file
	 * @param options copy options
	 * @throws IOException
	 */
	public void copy(final Path source, final Path target, final CopyOption... options) throws IOException {
		Files.walkFileTree(source, new FileVisitor<Path>() {


			private void copyIt(Path from, Path to) throws IOException {
				Files.copy(from, to, options);
			}

			private Path appendSkipRoot(Path lpath, Path rpath) {
				for (int i = 0; i < rpath.getNameCount(); i++) {
					lpath = lpath.resolve(rpath.getName(i).toString());
				}
				return lpath;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path newDir = appendSkipRoot(target, source.relativize(dir));
				// try to copy. if it fails, skip subtree
				try {
					copyIt(dir, newDir);
				} catch (IOException e) {
					if (!(e instanceof FileAlreadyExistsException)) return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path newFile = appendSkipRoot(target, source.relativize(file));
				// try to copy. if it fails ignore and continue
				try {
					copyIt(file, newFile);
				} catch (IOException ignored) {

				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc != null) throw exc;
				Path newDir = appendSkipRoot(target, source.relativize(dir));
				try {
					Files.setLastModifiedTime(newDir, Files.getLastModifiedTime(dir));
				} catch (IOException e) {
					if (!(e instanceof NoSuchFileException)) throw e;
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Delete a file. If it's a directory, recursively delete all sub-directories and sub-files. If a file cannot be deleted for some reason, an exception will be thrown.
	 *
	 * @param path the file to delete
	 * @throws IOException if I/O error happens
	 */
	public void deleteFile(String path) throws IOException {
		Files.walkFileTree(Paths.get(path), new FileVisitor<Path>() {
			private boolean deleteIt(Path p) throws IOException {
				return Files.deleteIfExists(p);
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				deleteIt(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				if (exc != null) throw exc;
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				deleteIt(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public boolean writeTextToFile(String text, String path, String encoding) {
		Objects.requireNonNull(text);
		if (path.trim().isEmpty() || encoding.trim().isEmpty())
			throw new IllegalArgumentException("blank strings are not allowed");

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path), Charset.forName(encoding))) {
			writer.write(text);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}
