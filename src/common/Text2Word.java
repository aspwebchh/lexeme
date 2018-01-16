package common;

import org.javatuples.KeyValue;
import org.wltea.analyzer.core.IKSegmenter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//从文本中提取词组
public class Text2Word {
    private List<String> getWordList( IKSegmenter ik ) throws IOException {
        List<String> list = new ArrayList<>();
        org.wltea.analyzer.core.Lexeme lex = ik.next();
        if( lex == null ) {
            return list;
        } else {
            list.add( lex.getLexemeText() );
            list.addAll( getWordList( ik ) );
            return list;
        }
    }

    private List<KeyValue<String,Integer>> compute( List<String> wordList ) {
        List<KeyValue<String, Integer>> result = new ArrayList<>();
        if( wordList.size() == 0 ) {
            return result;
        }
        String firstWord = wordList.get(0);
        List<String> matches = wordList.stream().filter((item)-> item.equals(firstWord)).collect(Collectors.toList());
        KeyValue<String,Integer> item = KeyValue.with(firstWord,matches.size());
        wordList.removeAll(matches);
        result.add(item);
        result.addAll(compute(wordList));
        return result;
    }

    public List<KeyValue<String,Integer>> convert(String text) {
        StringReader sr = new StringReader(text);
        IKSegmenter ik = new IKSegmenter(sr, true);
        List<KeyValue<String,Integer>> result = new ArrayList<>();
        try {
            List<String> wordList = this.getWordList( ik );
            result = compute(wordList);
            result.sort((a,b)->b.getValue() - a.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
