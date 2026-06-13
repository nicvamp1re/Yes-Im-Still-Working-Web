package com.yesimstillworking.service;

import com.yesimstillworking.entity.Patch;
import com.yesimstillworking.repository.PatchRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Service
public class PatchParserService {

    private final PatchRepository patchRepository;

    public PatchParserService(PatchRepository patchRepository) {
        this.patchRepository = patchRepository;
    }

    public void scrapeLatestPatches() {
        try {
            String mainUrl = "https://forums.kleientertainment.com/game-updates/oni-alpha/";
            String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

            Document mainDoc = Jsoup.connect(mainUrl)
                    .userAgent(userAgent)
                    .timeout(15000)
                    .get();

            // Select all rows explicitly
            Elements patchRows = mainDoc.select("li.cCmsRecord_row");
            System.out.println("Found " + patchRows.size() + " patch rows on the page.");

            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("MM/dd/")
                    .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
                    .toFormatter();

            for (Element row : patchRows) {
                try {
                    Element heading = row.selectFirst("h3.ipsType_sectionHead");
                    if (heading == null) continue;

                    String versionTitle = heading.ownText().trim();
                    if (versionTitle.isEmpty()) continue;

                    // If it already exists in H2 database, safely skip to next row instead of breaking
                    if (patchRepository.findByTitle(versionTitle).isPresent()) {
                        continue;
                    }

                    Element metaDiv = row.selectFirst("div.ipsDataItem_meta");
                    if (metaDiv == null) continue;

                    String cleanDateStr = metaDiv.text()
                            .replace("Released", "")
                            .replace("...", "")
                            .trim();

                    LocalDate releaseDate = LocalDate.parse(cleanDateStr, formatter);

                    Element linkAnchor = row.selectFirst("a.cRelease");
                    if (linkAnchor == null) continue;
                    String patchPageUrl = linkAnchor.attr("href");

                    // Deep crawling section
                    String innerDescription = "No patch notes extracted.";
                    try {
                        Document innerDoc = Jsoup.connect(patchPageUrl)
                                .userAgent(userAgent)
                                .timeout(10000)
                                .get();

                        // Locate this section inside your deep crawling block in PatchParserService.java
                        Element richTextSection = innerDoc.selectFirst("section.ipsType_richText");
                        if (richTextSection != null) {
                            // CHANGE THIS FROM .text() TO .html()
                            innerDescription = richTextSection.html().trim();
                        }
                    } catch (Exception innerEx) {
                        System.err.println("Failed inner page parse for: " + versionTitle);
                    }

                    // Map fields and write to relational DB
                    Patch patch = new Patch();
                    patch.setTitle(versionTitle);
                    patch.setReleaseDate(releaseDate);
                    patch.setDescription(innerDescription);

                    patchRepository.save(patch);
                    System.out.println("Saved patch: " + versionTitle);

                    Thread.sleep(500); // Small delay to avoid hammering the forum server
                } catch (Exception rowException) {
                    // Catch inner row errors so one bad row doesn't crash the whole sync task
                    System.err.println("Skipping a row due to parsing exception: " + rowException.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Master Sync Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}