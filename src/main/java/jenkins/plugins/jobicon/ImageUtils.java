/*
 *     Copyright 2012 Jean-Christophe Sirot <sirot@chelonix.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.jobicon;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;

import hudson.FilePath;
import jenkins.model.Jenkins;

/**
 * Utility functions for image manipulations.
 * 
 * @author Jean-Christophe Sirot
 */
class ImageUtils
{
	public static final String PATH = "customIcon";

	private ImageUtils()
	{
	}
	
	/**
	 * Resizes the image to 64x64 pixels and convert to PNG.
	 * @param in the original image
	 * @param out the converted image
	 * @param size the image size
	 * @throws IOException on I/O error
	 */
	static void resize(InputStream in, OutputStream out, int size) throws IOException
	{
		BufferedImage originalImage = ImageIO.read(in);
		BufferedImage resizedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, size, size, null);
		g.dispose();
		ImageIO.write(resizedImage, "png", out);
	}

	/**
	 * Supported icon sizes and the directory where icons are stored.
	 */
	enum Size {
		ORIGIN("origin", 0),
		SIZE_16("16x16", 16),
		SIZE_24("24x24", 24),
		SIZE_32("32x32", 32);

		public String directory;

		public int size;
		Size(String dir, int size)
		{
			this.directory = dir;
			this.size = size;
		}

		/**
		 * Tests whether the given size is valid. The size is given using the SSxSS notation.
		 * @param size the icon size
		 * @return {@code true} if the size is valid, {@code false} otherwise
		 */
		static boolean isValid(String size) {
			return "16x16".equals(size) | "24x24".equals(size) | "32x32".equals(size);
		}
	}

	/**
	 * Tests if an icon has already been uploaded
	 * @param filename  the icon filename with the extension
	 * @return {@code true} if the icon exists
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static boolean exists(String filename) throws IOException, InterruptedException
	{
		return Jenkins.getInstance().getRootPath().child("userContent")
				.child(PATH).child(Size.ORIGIN.directory).child(filename).exists();
	}

	private static void storeIcon(FilePath dir, ImageUtils.Size size, String name, byte[] data)
			throws IOException, InterruptedException
	{
		FilePath iconDir = dir.child(size.directory);
		iconDir.mkdirs();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		FilePath icon = iconDir.child(name);
		if (size != ImageUtils.Size.ORIGIN) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageUtils.resize(in, out, size.size);
			in = new ByteArrayInputStream(out.toByteArray());
		}
		icon.copyFrom(in);
		icon.chmod(0644);
	}

	static void storeIcon(String name, byte[] data) throws IOException, InterruptedException
	{
		FilePath iconDir = Jenkins.getInstance().getRootPath().child("userContent").child(PATH);
		storeIcon(iconDir, ImageUtils.Size.ORIGIN, name, data);
		storeIcon(iconDir, ImageUtils.Size.SIZE_16, name, data);
		storeIcon(iconDir, ImageUtils.Size.SIZE_24, name, data);
		storeIcon(iconDir, ImageUtils.Size.SIZE_32, name, data);
	}

	private static void deleteIcon(FilePath dir, ImageUtils.Size size, String name)
			throws IOException, InterruptedException
	{
		FilePath iconFile = dir.child(size.directory).child(name);
		if (iconFile.exists()) {
			iconFile.delete();
		}
	}

	/**
	 * Delete an icon.
	 * @param id the icon id
	 * @throws IOException
	 * @throws InterruptedException
	 */
	static void deleteIcon(String id) throws IOException, InterruptedException
	{
		FilePath iconDir = Jenkins.getInstance().getRootPath().child("userContent").child(PATH);
		deleteIcon(iconDir, ImageUtils.Size.ORIGIN, id + ".png");
		deleteIcon(iconDir, ImageUtils.Size.SIZE_16, id + ".png");
		deleteIcon(iconDir, ImageUtils.Size.SIZE_24, id + ".png");
		deleteIcon(iconDir, ImageUtils.Size.SIZE_32, id + ".png");
	}

	public static void moveIcon(FilePath icon) throws IOException, InterruptedException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		icon.copyTo(out);
		storeIcon(icon.getName(), out.toByteArray());
		icon.delete();
	}

	static String getIconURL(String iconFilename, String size)
	{
		if (! Size.isValid(size)) {
			size = Size.ORIGIN.directory;
		}
		return String.format("%s%s/%s/%s/%s", Jenkins.getInstance().getRootUrl(), "userContent",
				PATH, size, iconFilename);
	}
}