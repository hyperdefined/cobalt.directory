package lol.hyper.cobaltdirectory.web;

import lol.hyper.cobaltdirectory.CobaltDirectory;
import lol.hyper.cobaltdirectory.instance.Instance;
import lol.hyper.cobaltdirectory.utils.FileUtil;
import lol.hyper.cobaltdirectory.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WebBuilder {

    private static final Logger logger = LogManager.getLogger(WebBuilder.class);

    /**
     * Build the main instance page.
     *
     * @param instances     The instances to put on the page.
     * @param formattedDate The date to display.
     */
    public static void buildIndex(List<Instance> instances, String formattedDate) {
        String mainListTemplate = FileUtil.readFile(new File(CobaltDirectory.config.getString("web_path"), "template-mainlist.md"));
        if (mainListTemplate == null) {
            logger.error("Unable to read template-mainlist.md! Exiting...");
            System.exit(1);
        }
        // create the official, domain, and no domain tables
        String officialTable = StringUtil.buildInstanceTable(new ArrayList<>(instances), "official");
        String communityTable = StringUtil.buildInstanceTable(new ArrayList<>(instances), "community");
        // replace the placeholder with the tables
        mainListTemplate = mainListTemplate.replaceAll("<official-table>", officialTable);
        mainListTemplate = mainListTemplate.replaceAll("<community-table>", communityTable);
        mainListTemplate = mainListTemplate.replaceAll("<instance-count>", String.valueOf(instances.size()));
        // update the time it was run
        mainListTemplate = mainListTemplate.replaceAll("<time>", formattedDate);
        // write to index.md
        FileUtil.writeFile(mainListTemplate, new File(CobaltDirectory.config.getString("web_path"), "index.md"));
    }

    /**
     * Build an instance page for a given instance.
     *
     * @param instance      The instance to make the page for.
     * @param formattedDate The date to display.
     */
    public static void buildInstancePage(Instance instance, String formattedDate) {
        String instanceTemplate = FileUtil.readFile(new File(CobaltDirectory.config.getString("web_path"), "template-instance.md"));
        if (instanceTemplate == null) {
            logger.error("Unable to read template-instance.md! Exiting...");
            System.exit(1);
        }

        // replace different placeholders with values we want
        instanceTemplate = instanceTemplate.replaceAll("<hash>", instance.getHash());
        instanceTemplate = instanceTemplate.replaceAll("<time>", formattedDate);
        // display an access button or how to use the API
        // based on if the instance has web or not
        String instanceAccess;
        if (instance.getFrontEnd() == null) {
            instanceTemplate = instanceTemplate.replaceAll("<frontend>", instance.getApi());
            instanceAccess = "This instance does not have a web version. To use this instance, change your processing server <a href=\"https://cobalt.tools/settings/instances#community\">here</a> to <code>" + instance.getProtocol() + "://" + instance.getApi() + "</code>.";
        } else {
            if (instance.getApi().contains("imput.net")) {
                String imputServer = StringUtil.officialInstanceName(instance.getApi());
                instanceTemplate = instanceTemplate.replaceAll("<frontend>", instance.getFrontEnd() + " - " + imputServer);
            } else {
                instanceTemplate = instanceTemplate.replaceAll("<frontend>", instance.getFrontEnd());
            }
            instanceAccess = "<a href=\"" + instance.getProtocol() + "://" + instance.getFrontEnd() + "\"><button>Use Instance</button></a>";
        }

        instanceTemplate = instanceTemplate.replaceAll("<access-button>", instanceAccess);

        // create the score table to display the services
        String scoreTable = StringUtil.buildScoreTable(instance);
        // replace the placeholder with the score table
        instanceTemplate = instanceTemplate.replaceAll("<scores>", scoreTable);
        File scoreFile = new File(CobaltDirectory.config.getString("score_path"), instance.getHash() + ".md");
        FileUtil.writeFile(instanceTemplate, scoreFile);
    }

    /**
     * Build the page of instances for a given service.
     *
     * @param instances     The instances to put on the table.
     * @param formattedDate The date to display.
     * @param service       The service friendly name.
     * @param slug          The slug for the URL.
     */
    public static void buildServicePage(List<Instance> instances, String formattedDate, String service, String slug) {
        // sort into alphabetical order
        Collections.sort(instances);

        String serviceTemplate = FileUtil.readFile(new File(CobaltDirectory.config.getString("web_path"), "template-service.md"));
        if (serviceTemplate == null) {
            logger.error("Unable to read template-service.md! Exiting...");
            System.exit(1);
        }

        // create the official, domain, and no domain tables
        String officialTable = StringUtil.buildServiceTable(new ArrayList<>(instances), service, "official");
        String communityTable = StringUtil.buildServiceTable(new ArrayList<>(instances), service, "community");

        serviceTemplate = serviceTemplate.replaceAll("<time>", formattedDate);
        serviceTemplate = serviceTemplate.replaceAll("<service>", service);
        serviceTemplate = serviceTemplate.replaceAll("<service-slug>", slug);

        // replace the placeholder with the tables
        serviceTemplate = serviceTemplate.replaceAll("<service-official-table>", officialTable);
        serviceTemplate = serviceTemplate.replaceAll("<service-community-table>", communityTable);

        FileUtil.writeFile(serviceTemplate, new File(CobaltDirectory.config.getString("service_path"), slug + ".md"));
    }
}
