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

import java.util.List;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.util.ListBoxModel;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A Dashboard portlet which is similar to the standard dashboard plugin Jobs
 * Grid but includes the custom icon when configured.
 * 
 * @author Jean-Christophe Sirot
 */
public class CustomIconJobsPortlet extends DashboardPortlet {

  public static final int DEFAULT_COLUMN_COUNT = 3;
  private int columnCount = DEFAULT_COLUMN_COUNT;
	private String iconSize = "24x24";
	private boolean fillColumnFirst = false;

	@DataBoundConstructor
	public CustomIconJobsPortlet(
	        String name,
	        String iconSize,
	        int columnCount,
	        boolean fillColumnFirst) {
		super(name);
		this.iconSize = iconSize;
		this.columnCount = columnCount;
		this.fillColumnFirst = fillColumnFirst;
	}

	public int getColumnCount() {
		return this.columnCount <= 0 ? DEFAULT_COLUMN_COUNT : this.columnCount;
	}

	public String getIconSize() {
		return this.iconSize.isEmpty() ? "24x24" : this.iconSize;
	}

	public int getRowCount() {
		int s = this.getDashboard().getJobs().size();
		int rowCount = s / getColumnCount();
		if (s % getColumnCount() > 0) {
			rowCount += 1;
		}
		return rowCount;
	}

	public boolean getFillColumnFirst() {
		return this.fillColumnFirst;
	}

	public Job getJob(int curRow, int curColumun) {
		List<Job> jobs = this.getDashboard().getJobs();
		int idx = 0;
		// get grid coordinates from given params
		if (this.fillColumnFirst) {
			idx = curRow + curColumun * this.getRowCount();
			if (idx >= jobs.size()) {
				return null;
			}
		} else {
			idx = curColumun + curRow * getColumnCount();
			if (idx >= jobs.size()) {
				return null;
			}
		}
		return jobs.get(idx);
	}

	@Extension(optional = true)
	public static class DescriptorImpl extends Descriptor<DashboardPortlet> {
		@Override
		public String getDisplayName() {
			return Messages.Dashboard_jobsGridWithIcons();
		}

		public ListBoxModel doFillIconSizeItems() {
			ListBoxModel m = new ListBoxModel();
			m.add("16x16","16x16");
			m.add("24x24","24x24");
			m.add("32x32","32x32");
			return m;
		}
	}
}
