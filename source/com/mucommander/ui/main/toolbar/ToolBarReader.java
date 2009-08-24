/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main.toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mucommander.AppLogger;
import com.mucommander.RuntimeConstants;
import com.mucommander.file.AbstractFile;
import com.mucommander.io.BackupInputStream;
import com.mucommander.ui.action.ActionManager;

/**
 * This class parses the XML file describing the toolbar's buttons and associated actions.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ToolBarReader extends ToolBarIO {

    /** Temporarily used for XML parsing */
    private Vector actionsV;
    /** Temporarily used for XML parsing */
    private String fileVersion;
    
    /**
     * Starts parsing the XML description file.
     */
    ToolBarReader(AbstractFile descriptionFile) throws Exception {
        InputStream in;

        in = null;
        try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(descriptionFile), this);}
        finally {
            if(in != null) {
                try {in.close();}
                catch(IOException e) {}
            }
        }
    }
    
    public String[] getActionsRead() {
    	int nbActions = actionsV.size();
    	String[] actionIds = new String[nbActions];
        actionsV.toArray(actionIds);
        return actionIds;
    }

    ////////////////////////////
    // ContentHandler methods //
    ////////////////////////////

    public void startDocument() {
        actionsV = new Vector();
    }

    public void endDocument() {}

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals(BUTTON_ELEMENT)) {
        	// Resolve action id
        	String actionIdAttribute = attributes.getValue(ACTION_ID_ATTRIBUTE);
        	if (actionIdAttribute != null) {
        		if (ActionManager.isActionExist(actionIdAttribute))
        			actionsV.add(actionIdAttribute);
        		else
        			AppLogger.warning("Error in "+DEFAULT_TOOLBAR_FILE_NAME+": action id \"" + actionIdAttribute + "\" not found");
        	}
        	else {
        		// Resolve action class
        		String actionClassAttribute = attributes.getValue(ACTION_ATTRIBUTE);
        		String actionId = ActionManager.extrapolateId(actionClassAttribute);
        		if (ActionManager.isActionExist(actionId))
        			actionsV.add(actionId);
        		else
        			AppLogger.warning("Error in "+DEFAULT_TOOLBAR_FILE_NAME+": action id for class " + actionClassAttribute + " was not found");
        	}
        }
        else if(qName.equals(SEPARATOR_ELEMENT)) {
            actionsV.add(null);
        }
        else if (qName.equals(ROOT_ELEMENT)) {
        	// Note: early 0.8 beta3 nightly builds did not have version attribute, so the attribute may be null
            fileVersion = attributes.getValue(VERSION_ATTRIBUTE);

            // if the file's version is not up-to-date, update the file to the current version at quitting.
            if (!RuntimeConstants.VERSION.equals(fileVersion))
            	setModified();
        }
    }
}
