/*
 *     Copyright 2013 Jean-Christophe Sirot <sirot@chelonix.com>
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

import hudson.FilePath;
import hudson.Plugin;
import jenkins.model.Jenkins;

/**
 * Handle migration of data between versions
 *
 * @author Jean-Christophe Sirot
 */
public class CustomIconPlugin extends Plugin
{
	@Override
	public void start() throws Exception
	{
		super.start();
		FilePath path = Jenkins.getInstance().getRootPath().child("userContent").child(ImageUtils.PATH);
		if (path.exists()) {
			FilePath[] icons = path.list("*.png");
			for(FilePath icon: icons) {
				ImageUtils.moveIcon(icon);
			}
		}
	}
}
