import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.*;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    HashMap<String, List<PageEntry>> map = new HashMap<>();
    List<PageEntry> emptyList = new ArrayList<>();
    public BooleanSearchEngine(File pdfsDir) throws IOException {
        for (File pdf : pdfsDir.listFiles()) {
            var doc = new PdfDocument(new PdfReader(pdf));
            var pageCount = doc.getNumberOfPages(); // Количество страниц в документе

            for (int i = 1; i <= pageCount; i++) {
                var page = doc.getPage(i);
                var text = PdfTextExtractor.getTextFromPage(page);
                var words = text.split("\\P{IsAlphabetic}+");

                emptyList.add(new PageEntry(pdf.getName(), i, 0));

                Map<String, Integer> freqs = new HashMap<>(); // мапа, где ключом будет слово, а значением - частота
                for (var word : words) { // перебираем слова
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }

                for (String s : freqs.keySet()) {
                    Set<String> ss = map.keySet();
                    if (ss.contains(s)) {
                        map.get(s).add(new PageEntry(pdf.getName(), i, freqs.get(s)));
                    } else {
                        ArrayList<PageEntry> list = new ArrayList<>();
                        list.add(new PageEntry(pdf.getName(), i, freqs.get(s)));
                        map.put(s, list);
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) throws IOException {
        String[] parts = word.split("\\P{IsAlphabetic}+");
        List<String> searchList = new ArrayList<>(Arrays.asList(parts));

        List<String> stopList = new ArrayList<>();

        File file = new File("stop-ru.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while(reader.ready()) {
                stopList.add(reader.readLine());
        }
        } catch (IOException e) {
            e.getMessage();
        }

        searchList.removeAll(stopList);
        ArrayList<List<PageEntry>> lst = new ArrayList<>();
        
        for (int i = 0; i < searchList.size(); i++) {
            lst.add(map.get(searchList.get(i).toLowerCase()));
        }
        var count = 0;
        ArrayList<PageEntry> result = new ArrayList<>();
        for (PageEntry entry : emptyList) {
            for (List<PageEntry> pageEntries : lst) {
                for (int i = 0; i < pageEntries.size(); i++) {
                    if (entry.getPdfName().equals(pageEntries.get(i).getPdfName()) && entry.getPage() == pageEntries.get(i).getPage()) {
                        count += pageEntries.get(i).getCount();
                }
            }
        }
            if (count != 0) {
                result.add(new PageEntry(entry.getPdfName(), entry.getPage(), count));
            }
            count = 0;
    }
        Collections.sort(result);
        return result;
    }
}
