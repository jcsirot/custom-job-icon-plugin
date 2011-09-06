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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.ServletException;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * This property holds the custom icon filename.
 * 
 * @author Jean-Christophe Sirot
 */
public class CustomIconProperty extends JobProperty<Job<?, ?>>
{
	public static final String PATH = "customIcon";
	
	public final String name;

	@DataBoundConstructor
	public CustomIconProperty(String name)
	{
		this.name = name;
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
				String name = formData.getJSONObject("jobicon").getString("name");
				if (new File(name).isAbsolute()) {
					throw new FormException(Messages.Config_absolute(), "name");
				}
				return req.bindJSON(CustomIconProperty.class, 
					formData.getJSONObject("jobicon"));
			}
			return null;
		}

		/**
		 * Validates the icon filename form.
		 * @param job the current configured {@code Job}
		 * @param name the filename to be validated
		 * @return the validation
		 */
		public FormValidation doCheckName(@AncestorInPath Job job, @QueryParameter String name) 
				throws IOException, InterruptedException
		{
			File sub = new File(name);
			if (sub.isAbsolute()) {
				return FormValidation.error(Messages.Config_absolute());
			}
			FilePath path = new FilePath(job.getRootDir()).child(CustomIconProperty.PATH);
			return path.validateRelativePath(name, true, true);
			
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
		 * Upload an image file.
		 * @param req the stapler request
		 * @param rsp the stapler response
		 * @param job the job name
		 */
		public void doUpload(StaplerRequest req, StaplerResponse rsp,
			@QueryParameter String job)
			throws IOException, ServletException, InterruptedException
		{
			Jenkins jenkins = Jenkins.getInstance();
			AbstractProject prj = (AbstractProject) jenkins.getItem(job);
			prj.checkPermission(Item.CONFIGURE);
			FileItem file = req.getFileItem("jobicon.file");
			String error = null;
			String filename = null;
			if (file == null || file.getName().isEmpty()) {
				error = Messages.Upload_noFile();
			} else {
				// sanitize filename
				filename = file.getName().replaceFirst(".*/", "").replaceAll("[^\\w.,;:()#@!=+-]", "_");
				FilePath iconDir = new FilePath(prj.getRootDir()).child("customIcon");
				iconDir.mkdirs();
				FilePath icon = iconDir.child(filename);//new File(prj.getRootDir(), filename);
				if (icon.exists()) {
					error = Messages.Upload_dup();
				}
				icon.copyFrom(file);
				icon.chmod(0644);
			}
			rsp.setContentType("text/html");
			rsp.setContentType("text/html");
			rsp.getWriter().println(
				(error != null ? error : Messages.Upload_done("<tt>" + filename + "</tt>"))
				+ " <a href=\"javascript:history.back()\">" + Messages.Upload_back() + "</a>");
		}
	}
}
