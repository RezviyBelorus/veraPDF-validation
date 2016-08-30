package org.verapdf.model.impl.pd.font;

import org.apache.log4j.Logger;
import org.verapdf.as.ASAtom;
import org.verapdf.as.io.ASInputStream;
import org.verapdf.cos.COSDictionary;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSStream;
import org.verapdf.font.PDFlibFont;
import org.verapdf.font.cff.CFFCidFont;
import org.verapdf.font.cff.CFFFont;
import org.verapdf.font.truetype.TrueTypeFont;
import org.verapdf.model.baselayer.Object;
import org.verapdf.model.coslayer.CosStream;
import org.verapdf.model.factory.operators.RenderingMode;
import org.verapdf.model.impl.cos.GFCosStream;
import org.verapdf.model.pdlayer.PDCIDFont;
import org.verapdf.pd.PDFont;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/**
 * Represents CID Font dictionary.
 *
 * @author Sergey Shemyakov
 */
public class GFPDCIDFont extends GFPDFont implements PDCIDFont {

    private static final Logger LOGGER = Logger.getLogger(GFPDCIDFont.class);

    public static final String CID_FONT_TYPE = "PDCIDFont";

    public static final String CID_SET = "CIDSet";

    public static final String IDENTITY = "Identity";
    public static final String CUSTOM = "Custom";
    private PDFAFlavour flavour;

    public GFPDCIDFont(PDFont font, RenderingMode renderingMode, PDFAFlavour flavour) {
        super(font, renderingMode, CID_FONT_TYPE);
        this.flavour = flavour;
    }

    @Override
    public List<? extends Object> getLinkedObjects(String link) {
        if (CID_SET.equals(link)) {
            return this.getCIDSet();
        }
        return super.getLinkedObjects(link);
    }

    /**
     * @return link to the stream containing the value of the CIDSet entry in
     * the CID font descriptor dictionary.
     */
    private List<CosStream> getCIDSet() {
        COSStream cidSet = (COSStream)
                this.pdFont.getFontDescriptor().getKey(ASAtom.CID_SET).get();
        if (cidSet != null) {
            List<CosStream> list = new ArrayList<>(MAX_NUMBER_OF_ELEMENTS);
            list.add(new GFCosStream(cidSet));
            return Collections.unmodifiableList(list);
        }
        return Collections.emptyList();
    }

    /**
     * @return string representation of the CIDtoGIDMap entry ("Identity", or
     * "Custom" in case of stream value).
     */
    @Override
    public String getCIDToGIDMap() {
        COSObject cidToGidObject = this.pdFont.getDictionary().getKey(
                ASAtom.CID_TO_GID_MAP);
        if (cidToGidObject.getType() == COSObjType.COS_STREAM) {
            return CUSTOM;
        }
        if (cidToGidObject.getType() == COSObjType.COS_NAME &&
                IDENTITY.equals(cidToGidObject.getString())) {
            return IDENTITY;
        }
        return null;
    }

    /**
     * @return true if the CIDSet is present and correctly lists all glyphs
     * present in the embedded font program.
     */
    @Override
    public Boolean getcidSetListsAllGlyphs() {
        try {
            COSStream cidSet = getCIDSetStream();
            if (cidSet != null) {
                ASInputStream stream = cidSet.getData(COSStream.FilterFlags.DECODE);
                long length = cidSet.getLength();
                byte[] cidSetBytes = getCIDsFromCIDSet(stream, length);

                //reverse bit order in bit set (convert to big endian)
                BitSet bitSet = toBitSetBigEndian(cidSetBytes);

                PDFlibFont cidFont = this.pdFont.getFontFile();

                for (int i = 1; i < bitSet.size(); i++) {
                    if (bitSet.get(i) && !cidFont.containsCID(i)) {
                        return Boolean.FALSE;
                    }
                }

                if (!flavour.equals(PDFAFlavour.PDFA_1_A) || !flavour.equals(PDFAFlavour.PDFA_1_B)) {
                    //on this levels we need to ensure that all glyphs present in font program are described in cid set
                    if (cidFont instanceof CFFFont && ((CFFFont) cidFont).isCIDFont()) {
                        CFFCidFont cffCidFont = (CFFCidFont) ((CFFFont) cidFont).getFont();
                        if (bitSet.cardinality() < cffCidFont.getNGlyphs()) {
                            return Boolean.FALSE;
                        }
                    } else if (cidFont instanceof TrueTypeFont) {
                        if (bitSet.cardinality() < ((TrueTypeFont) cidFont).getNGlyphs()) {
                            return Boolean.FALSE;
                        }
                    }
                }

            }
        } catch (IOException e) {
            LOGGER.debug("Error while parsing embedded font program. " + e.getMessage(), e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private COSStream getCIDSetStream() {
        COSDictionary fontDescriptor = this.pdFont.getFontDescriptor();
        COSStream cidSet;
        if (fontDescriptor != null) {
            cidSet = (COSStream) fontDescriptor.getKey(ASAtom.CID_SET).get();
            return cidSet;
        }
        return null;
    }

    private byte[] getCIDsFromCIDSet(ASInputStream cidSet, long length) throws IOException {
        byte[] cidSetBytes = new byte[(int) length];
        if (cidSet.read(cidSetBytes) != length) {
            LOGGER.debug("Did not read necessary number of cid set bytes");
        }
        return cidSetBytes;
    }

    private BitSet toBitSetBigEndian(byte[] source) {
        BitSet bitSet = new BitSet(source.length * 8);
        int i = 0;
        for (int j = 0; j < source.length; j++) {
            int b = source[j] >= 0 ? source[j] : 256 + source[j];
            for (int k = 0; k < 8; k++) {
                bitSet.set(i++, (b & 0x80) != 0);
                b = b << 1;
            }
        }

        return bitSet;
    }
}
