/*
 *     Copyright 2011-2013 Jean-Christophe Sirot <sirot@chelonix.com>
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

import hudson.model.Action;
import hudson.model.Job;

import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * This action add {@code /customIcon/} to the job URL space and serve the
 * icon image.
 * 
 * This action accepts the query parameter {@code size} with these
 * acceptable values {@code 16x16}, {@code 24x24} and {@code 32x32}.
 * 
 * @author Jean-Christophe Sirot
 */
public class CustomIconAction implements Action
{
	private final Job job;

	/**
	 * Creates a new {@code CustomIconAction}.
	 * 
	 * @param job the owner job
	 */
	public CustomIconAction(Job job)
	{
		this.job = job;
	}

	@Override
	public String getIconFileName()
	{
		return null;
	}

	@Override
	public String getDisplayName()
	{
		return "Custom Icon";
	}

	@Override
	public String getUrlName()
	{
		return "customIcon";
	}

	/**
	 * Handles the action call.
	 * 
	 * @param req  the stapler request
	 * @param rsp  the stapler response
	 */
	public void doDynamic(StaplerRequest req, StaplerResponse rsp)
			throws IOException, ServletException, InterruptedException
	{
		CustomIconProperty prop = (CustomIconProperty) job.getProperty(CustomIconProperty.class);
		String iconFilename = prop.iconfile;
		String size = req.getParameter("size");
		rsp.sendRedirect(ImageUtils.getIconURL(iconFilename, size));
	}

}
