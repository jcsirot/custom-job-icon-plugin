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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;

/**
 * Utility functions for images.
 * 
 * @author Jean-Christophe Sirot
 */
class ImageUtils
{
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
}
