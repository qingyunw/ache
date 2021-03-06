package focusedCrawler.tools;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;

import focusedCrawler.target.model.TargetModelJson;
import focusedCrawler.target.repository.FilesTargetRepository;
import focusedCrawler.target.repository.FilesTargetRepository.RepositoryIterator;
import focusedCrawler.util.CliTool;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name="DumpMetadataFromRepository")
public class DumpMetadataFromRepository extends CliTool {

    @Option(name = "--input-path",
            required = true,
            description = "Path to directory containing a FILES repository")
    private String inputPath;

    @Option(name = "--output-file",
            required = true,
            description = "Text file containing metadata")
    private String outputFile;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new DumpMetadataFromRepository());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading URLs from path: " + inputPath);
        
        FilesTargetRepository repository = new FilesTargetRepository(inputPath);
        ObjectMapper jsonMapper = new ObjectMapper();
        
        PrintStream out = System.out;
        if(outputFile != null) {
            out  = new PrintStream(outputFile);
        }
        
        int totalPages = 0;
        RepositoryIterator it = repository.iterator();
        while(it.hasNext()) {
            TargetModelJson page = it.next();

            String id = Hashing.sha256().hashBytes(page.getUrl().getBytes()).toString();
            String signature = Hashing.md5().hashBytes(page.getContent()).toString();
            
            Map<String, Object> obj = new HashMap<String, Object>();
            obj.put("id", id);
            obj.put("url", page.getUrl());
            obj.put("signature", signature);
            obj.put("score", page.getRelevance().getRelevance());
            obj.put("team", "NYU");
            obj.put("timestamp_fetch", page.getFetchTime());
            
            out.println(jsonMapper.writeValueAsString(obj));
            
            totalPages++;
            if (totalPages % 1000 == 0) {
                System.out.printf("Processed %s pages...\n", totalPages);
            }
        }
        repository.close();
        System.out.println("done.");
    }

}
