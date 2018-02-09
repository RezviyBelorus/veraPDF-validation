/**
 * This file is part of validation-model, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * validation-model is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with validation-model as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * validation-model as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.gf.model.impl.containers;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSKey;
import org.verapdf.gf.model.impl.pd.colors.GFPDSeparation;
import org.verapdf.gf.model.impl.pd.util.TaggedPDFRoleMapHelper;
import org.verapdf.model.operator.Glyph;
import org.verapdf.model.pdlayer.PDColorSpace;
import org.verapdf.model.pdlayer.PDFont;
import org.verapdf.pd.PDDocument;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.util.*;

/**
 * @author Timur Kamalov
 */
public class StaticContainers {

	//Test comment, should be deleted
	private static PDDocument document;
	private static PDFAFlavour flavour;

	// TaggedPDF
	public static TaggedPDFRoleMapHelper roleMapHelper;

	//PBoxPDSeparation
	public static Map<String, List<GFPDSeparation>> separations = new HashMap<>();
	public static List<String> inconsistentSeparations = new ArrayList<>();

	//ColorSpaceFactory
	public static Map<String, PDColorSpace> cachedColorSpaces = new HashMap<>();

	//FontFactory
	public static Map<String, PDFont> cachedFonts = new HashMap<>();

	public static Set<COSKey> fileSpecificationKeys = new HashSet<>();

	public static Stack<COSKey> transparencyVisitedContentStreams = new Stack<>();
	public static boolean validPDF  = true;

	public static Map<String, Glyph> cachedGlyphs = new HashMap<>();

	public static void clearAllContainers() {
		if (document != null) {
			document = null;
		}
		flavour = null;
		roleMapHelper = null;
		separations = new HashMap<>();
		inconsistentSeparations = new ArrayList<>();
		cachedColorSpaces = new HashMap<>();
		cachedFonts = new HashMap<>();
		fileSpecificationKeys = new HashSet<>();
		transparencyVisitedContentStreams = new Stack<>();
		cachedGlyphs = new HashMap<>();
		validPDF = true;
	}

	public static PDDocument getDocument() {
		return document;
	}

	public static void setDocument(PDDocument document) {
		StaticContainers.document = document;
	}

	public static PDFAFlavour getFlavour() {
		return flavour;
	}

	public static void setFlavour(PDFAFlavour flavour) {
		StaticContainers.flavour = flavour;
	}

	public static TaggedPDFRoleMapHelper getRoleMapHelper() {
		return roleMapHelper;
	}

	public static void setRoleMapHelper(Map<ASAtom, ASAtom> roleMap) {
		StaticContainers.roleMapHelper = new TaggedPDFRoleMapHelper(roleMap);
	}
}
