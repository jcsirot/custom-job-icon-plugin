/*
 *     Copyright 2011 Jean-Christophe Sirot <sirot@chelonix.com>
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.fileupload.FileItem;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.lang.Klass;

/**
 * This property holds the custom icon filename.
 *
 * @author Jean-Christophe Sirot
 */
public class CustomIconProperty extends JobProperty<Job<?, ?>>
{
	public static final String PATH = "customIcon";

	public final String iconfile;

	@DataBoundConstructor
	public CustomIconProperty(String iconfile)
	{
		this.iconfile = iconfile;
	}

	@Override
	public Collection<CustomIconAction> getJobActions(Job job)
	{
		return Arrays.asList(new CustomIconAction(job));
	}

	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor
	{
		@Override
		public String getDisplayName()
		{
			return "Custom icon";
		}

		@Override
		public boolean isApplicable(Class<? extends Job> jobType)
		{
			return true;
		}

		@Override
		public CustomIconProperty newInstance(StaplerRequest req,
			JSONObject formData) throws FormException
		{
			if (formData.has("jobicon")) {
				if (!formData.getJSONObject("jobicon").has("iconfile")) {
					throw new FormException(Messages.Config_missing(), "iconfile");
				}
				return req.bindJSON(CustomIconProperty.class,
					formData.getJSONObject("jobicon"));
			}
			return null;
		}

		/**
		 * Serves the upload form
		 * @param req the stapler request
		 * @param rsp the stapler response
		 */
		public void doStartUpload(StaplerRequest req, StaplerResponse rsp)
				throws IOException, ServletException
		{
			rsp.setContentType("text/html");
			req.getView(CustomIconProperty.class, "startUpload.jelly").forward(req, rsp);
		}

		/**
		 * Serves the icon table snippet
		 * @param req the stapler request
		 * @param rsp the stapler response
		 */
		public void doGlobalIconsTable(StaplerRequest req, StaplerResponse rsp)
				throws IOException, ServletException
		{
			rsp.setContentType("text/html");
			((RequestImpl)req).getView(Klass.java(CustomIconProperty.class), this, "global-icons-table.jelly").forward(req, rsp);
		}

		/**
		 * Delete an icon. The request parameter {@code icon} contains the id.
		 * @param req the stapler request
		 * @param rsp the stapler response
		 */
		public void doDeleteIcon(StaplerRequest req, StaplerResponse rsp)
				throws IOException, ServletException, InterruptedException
		{
			deleteIcon(req.getParameter("icon"));
			doGlobalIconsTable(req, rsp);
		}

		/**
		 * Upload an image file.
		 * @param req the stapler request
		 * @param rsp the stapler response
		 * @param job the job name
		 */
		public void doUpload(StaplerRequest req, StaplerResponse rsp,
			@QueryParameter String job)
			throws IOException, ServletException, InterruptedException,
			NoSuchAlgorithmException
		{
			Jenkins jenkins = Jenkins.getInstance();
			jenkins.checkPermission(Jenkins.ADMINISTER);
			FileItem file = req.getFileItem("jobicon.file");
			String error = null;
			if (file == null || file.getName().isEmpty()) {
				error = Messages.Upload_noFile();
			} else {
				// sanitize filename
				//filename = file.getName().replaceFirst(".*/", "").replaceAll("[^\\w.,;:()#@!=+-]", "_");
				MessageDigest dg = MessageDigest.getInstance("SHA1");
				String filename = Hex.encodeHexString(dg.digest(file.get())) + ".png";
				FilePath iconDir = jenkins.getRootPath().child("userContent").child(PATH);
				iconDir.mkdirs();
				FilePath icon = iconDir.child(filename);
				if (icon.exists()) {
					error = Messages.Upload_dup();
				}
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageUtils.resize(file.getInputStream(), out, 64);
				icon.copyFrom(new ByteArrayInputStream(out.toByteArray()));
				icon.chmod(0644);
			}
			rsp.setContentType("text/html");
			rsp.getWriter().println(
				(error != null ? error : Messages.Upload_done())
				+ " <a href=\"javascript:history.back()\">" + Messages.Upload_back() + "</a>");
		}

		/**
		 * Retrieves the list of available icons. Sort the icons by filenames.
		 * @return the list of icon filenames
		 * @throws IOException if an error occurs while reading the icons directory
		 * @throws InterruptedException
		 */
		public List<String> getIcons() throws IOException, InterruptedException
		{
			FilePath iconDir = Jenkins.getInstance().getRootPath()
					.child("userContent").child(PATH);
			if (!iconDir.exists()) {
				return Collections.EMPTY_LIST;
			}
			List<FilePath> files = iconDir.list();
			List<String> names = new ArrayList<String>();
			for (FilePath fp: files) {
				names.add(fp.getBaseName());
			}
            Collections.sort(names);
			return names;
		}

		/**
		 * Delete an icon.
		 * @param id the icon id
		 * @throws IOException
		 * @throws InterruptedException 
		 */
		private void deleteIcon(String id) throws IOException, InterruptedException
		{
			FilePath iconFile = Jenkins.getInstance().getRootPath()
				.child("userContent").child(PATH)
				.child(id + ".png");
			if (iconFile.exists()) {
				iconFile.delete();
			}
		}

		/**
		 * Indicates if any icon has been loaded.
		 *
		 * @return {@code true} if no icon is available, {@code false} otherwise
		 * @throws IOException if an error occurs while reading the icons directory
		 * @throws InterruptedException
		 */
		public boolean isIconListEmpty() throws IOException, InterruptedException
		{
			return getIcons().isEmpty();
		}

		/**
		 * Return a matrix of icon filenames. This matrix is used to display
		 * the table of available icons in the job configuration page.
		 *
		 * @param colCount the number of columns of the matrix.
		 * @return the icon filenames as a matrix (as a list of rows)
		 * @throws IOException if an error occurs while reading the icons directory
		 * @throws InterruptedException
		 */
		public List<List<String>> getIconsAsListOfList(int colCount)
				throws IOException, InterruptedException
		{
			List<String> icons = getIcons();
			List<List<String>> split = new ArrayList<List<String>>();
			List<String> tmp = null;
			for (int i = 0; i < icons.size(); i++) {
				if (i % colCount == 0) {
					tmp = new ArrayList<String>();
					split.add(tmp);
				}
				tmp.add(icons.get(i));
			}
			return split;
		}
	}
}
