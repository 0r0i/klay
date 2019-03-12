package klay.es.plugin.analysis;

import klay.core.Klay;
import klay.core.morphology.analysis.Morph;
import klay.core.morphology.analysis.Morphs;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.RollingCharBuffer;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.util.Iterator;

public final class KlayTokenizer extends Tokenizer {

    private final Klay klay;

    private Iterator<Morph> morphIterator;

    private final RollingCharBuffer buffer = new RollingCharBuffer();

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public KlayTokenizer(Klay klay) {
        this.klay = klay;
    }

    private final int MAX_READ_COUNT = 10;
    private int readCount = 0;
    @Override
    public boolean incrementToken() throws IOException {
        if(morphIterator == null || !morphIterator.hasNext()) {
            try {
                boolean readSome = readNewData();
                if (!readSome) return false;
            } catch (AssertionError e) {
                return false;
            }
        }

        if(morphIterator.hasNext()) {
            Morph morph = morphIterator.next();
            termAtt.setEmpty().append(morph.getText());
            return true;
        }

        return false;
    }

    private boolean readNewData() throws IOException {
        int ch;
        StringBuilder sb = new StringBuilder();
        while((ch = buffer.get(readCount++)) > 0) {
            sb.append((char) ch);
            if(readCount >= MAX_READ_COUNT) break;
        }
        if(sb.length() == 0) return false;

        Morphs morphs = klay.doKlay(sb.toString());
        morphIterator = morphs.iterator();
        return true;
    }

    @Override
    public void close() throws IOException {
        super.close();
        buffer.reset(input);
        morphIterator = null;
        readCount = 0;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        buffer.reset(input);
        morphIterator = null;
        readCount = 0;
    }
}
