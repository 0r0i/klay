package da.klay.dictionary.triebase.system;

import da.klay.common.dictionary.structure.TrieResult;
import da.klay.common.parser.JasoParser;
import da.klay.dictionary.param.DictionaryBinarySource;
import da.klay.dictionary.param.DictionaryBinaryTarget;
import da.klay.dictionary.param.DictionaryTextSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EmissionTrieBaseDictionaryTest2 {

    static EmissionTrieBaseDictionary etd;
    @BeforeAll
    static void before() throws Exception {
        Path path1 = Paths.get("src/test/resources/test.dic.word");
        Path path2 = Paths.get("src/test/resources/test.dic.irregular");
        etd = new EmissionTrieBaseDictionary(
                new DictionaryTextSource[]{
                        new DictionaryTextSource(path1, StandardCharsets.UTF_8),
                        new DictionaryTextSource(path2, StandardCharsets.UTF_8)});
    }

    @Test
    void get() {
        // 1. getFully test
        String key = JasoParser.parseAsString("대구일보");
        CharSequence result = etd.getFully(key);

        assertNotNull(result);

        // 2. partially matching test
        key = JasoParser.parseAsString("대구일보구로구");
        TrieResult trieResult = etd.getLastOnPath(key);

        assertEquals(true, trieResult.hasResult());
        assertEquals(8, trieResult.getEndPosition());

        trieResult = etd.getLastOnPath(key, trieResult.getEndPosition()+1);

        assertEquals(true, trieResult.hasResult());
        assertEquals(9, trieResult.getStartPosition());
        assertEquals(14, trieResult.getEndPosition());

        // 3. all matching test
        key = JasoParser.parseAsString("대구일보기자");
        TrieResult[] results = etd.getAll(key);

        assertEquals(2, results.length);
        for(TrieResult res : results)
            assertEquals(true, res.hasResult());

        key = JasoParser.parseAsString("하였거나");
        result = etd.getFully(key);

        assertEquals(true, result != null);
    }

    @Test
    void saveAndLoadBinary() throws Exception {

        Path filePath = Paths.get("src/test/resources/binary/test.emission2.bin");
        if(Files.notExists(filePath)) etd.save(new DictionaryBinaryTarget(filePath));

        EmissionTrieBaseDictionary newETD = new EmissionTrieBaseDictionary(new DictionaryBinarySource(filePath));

        // 1. getFully test
        String key = JasoParser.parseAsString("대구일보");
        CharSequence result = newETD.getFully(key);

        assertNotNull(result);
    }
}