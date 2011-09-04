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

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.views.ListViewColumn;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * A {@link ListViewColumn} which displays the job custom icon if configured.
 * 
 * @author Jean-Christophe Sirot
 */
public class CustomIconColumn extends ListViewColumn
{
	@Extension
	public static class CustomIconColumnDescriptor extends Descriptor<ListViewColumn>
	{
		@Override
		public String getDisplayName()
		{
			return "Custom Icon";
		}

		@Override
		public ListViewColumn newInstance(final StaplerRequest request,
				final JSONObject formData) throws FormException
		{
			return new CustomIconColumn();
		}
	}
}
