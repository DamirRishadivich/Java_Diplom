import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    protected HashMap<String, List<PageEntry>> pageEntryMap = new HashMap<>();
    protected List<PageEntry> emptyList = new ArrayList<>();
    protected List<String> stopList = new ArrayList<>();

    public BooleanSearchEngine(File pdfsDir, File stopWordListPath) throws IOException {
        for (File pdf : pdfsDir.listFiles()) {
            var doc = new PdfDocument(new PdfReader(pdf));
            var pageCount = doc.getNumberOfPages();

            try (BufferedReader reader = new BufferedReader(new FileReader(new File("stop-ru.txt")))) {
                while (reader.ready()) {
                    stopList.add(reader.readLine());
                }
            } catch (IOException e) {
                e.getMessage();
            }

            for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
                var page = doc.getPage(pageNumber);
                var text = PdfTextExtractor.getTextFromPage(page);
                var words = text.split("\\P{IsAlphabetic}+");

                emptyList.add(new PageEntry(pdf.getName(), pageNumber, 0));

                Map<String, Integer> freqs = new HashMap<>(); // мапа, где ключом будет слово, а значением - частота
                for (var word : words) { // перебираем слова
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }

                for (String uniqueWord : freqs.keySet()) {
                    Set<String> uniqueWordCheck = pageEntryMap.keySet();
                    if (uniqueWordCheck.contains(uniqueWord)) {
                        pageEntryMap.get(uniqueWord).add(new PageEntry(pdf.getName(), pageNumber, freqs.get(uniqueWord)));
                    } else {
                        List<PageEntry> resultPageEntryList = new ArrayList<>();
                        resultPageEntryList.add(new PageEntry(pdf.getName(), pageNumber, freqs.get(uniqueWord)));
                        pageEntryMap.put(uniqueWord, resultPageEntryList);
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) throws IOException {
        String[] parts = word.split("\\P{IsAlphabetic}+");
        List<String> searchList = new ArrayList<>(Arrays.asList(parts));
        searchList.removeAll(stopList);
        List<List<PageEntry>> pageEntryList = new ArrayList<>();
        
        for (int i = 0; i < searchList.size(); i++) {
            pageEntryList.add(pageEntryMap.get(searchList.get(i).toLowerCase()));
        }
        var count = 0;
        ArrayList<PageEntry> resultPageEntryList = new ArrayList<>();
        for (PageEntry entry : emptyList) {
            for (List<PageEntry> pageEntries : pageEntryList) {
                for (int i = 0; i < pageEntries.size(); i++) {
                    if (entry.getPdfName().equals(pageEntries.get(i).getPdfName()) && entry.getPage() == pageEntries.get(i).getPage()) {
                        count += pageEntries.get(i).getCount();
                    }
                }
            }
            if (count != 0) {
                resultPageEntryList.add(new PageEntry(entry.getPdfName(), entry.getPage(), count));
            }
            count = 0;
        }
        Collections.sort(resultPageEntryList);
        return resultPageEntryList;
    }
}
