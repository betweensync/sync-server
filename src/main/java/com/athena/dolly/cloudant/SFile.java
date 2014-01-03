/* 
 * Athena Dolly Project 
 * 
 * Copyright (C) 2013 Open Source Consulting, Inc. All rights reserved by Open Source Consulting, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * Jerry Jung		2013. 12. 30.		First Draft.
 */
package com.athena.dolly.cloudant;

import java.io.File;

public class SFile {
	public String _id;
	public String _rev;
	public String _deleted;
	public String absolutePath;
	public String parent;
	public String name;
	public long lastModified;
	public boolean isDirectory;

	public SFile() {

	}

	public SFile(File f) {
		this.absolutePath = f.getAbsolutePath();
		this.isDirectory = f.isDirectory();
		this.name = f.getName();
		this.lastModified = f.lastModified();
		this.parent = f.getParent();
	}

	@Override
	public String toString() {
		return "SFile [_id=" + _id + ", _rev=" + _rev + ", _deleted=" + _deleted + ", absolutePath=" + absolutePath
				+ ", parent=" + parent + ", name=" + name + ", lastModified=" + lastModified + ", isDirectory="
				+ isDirectory + "]";
	}

}