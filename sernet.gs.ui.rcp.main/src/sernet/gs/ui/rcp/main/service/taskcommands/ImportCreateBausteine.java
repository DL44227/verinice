/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbMassn;
import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.importData.BausteinInformationTransfer;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.GefaehrdungInformationTransfer;
import sernet.gs.reveng.importData.MassnahmeInformationTransfer;
import sernet.gs.scraper.GSScraper;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.gsimport.ImportKostenUtil;
import sernet.gs.ui.rcp.gsimport.TransferData;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIConfig;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementBuildException;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateBaustein;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.gstoolimport.MassnahmenFactory;

/**
 * Create BausteinUmsetzung objects during import for given target object and
 * assigned Bausteine from source database.
 *
 *
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class ImportCreateBausteine extends GenericCommand {

    private transient Logger log = Logger.getLogger(ImportCreateBausteine.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ImportCreateBausteine.class);
        }
        return log;
    }

    // attention dear reader: ud is an abbreviation for [u]ser[d]efined

    private CnATreeElement element;
    private Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap;
    private Map<BausteinUmsetzung, List<BausteineMassnahmenResult>> individualMassnahmenMap;
    private Map<MbBaust, BausteinInformationTransfer> udBausteineTxtMap;
    private Map<MbMassn, MassnahmeInformationTransfer> udBstMassTxtMap;
    private Map<MbBaust, List<GefaehrdungInformationTransfer>> udBaustGefMap;
    private boolean importUmsetzung;
    private boolean kosten;
    private Map<MbBaust, BausteinUmsetzung> alleBausteineToBausteinUmsetzungMap;
    private Map<MbBaust, ModZobjBst> alleBausteineToZoBstMap;

    private List<MbZeiteinheitenTxt> zeiten;
    private Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmen;
    private List<Baustein> bausteine;
    private String sourceId;

    private Map<MbBaust, Baustein> gstool2veriniceBausteinMap;

    private static final short BST_BEARBEITET_ENTBEHRLICH = 3;


    public ImportCreateBausteine(String sourceId, CnATreeElement element,
            Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap,
            List<MbZeiteinheitenTxt> zeiten, boolean kosten, boolean importUmsetzung,
            IBSIConfig bsiConfig, Map<MbBaust, BausteinInformationTransfer> udBstTxtMap, Map<MbMassn, MassnahmeInformationTransfer> udBstMassTxtMap,
            Map<MbBaust, List<GefaehrdungInformationTransfer>> udBaustGefMap, List<Baustein> bausteine) {
        this.element = element;
        this.bausteineMassnahmenMap = bausteineMassnahmenMap;
        this.kosten = kosten;
        this.importUmsetzung = importUmsetzung;
        this.zeiten = zeiten;
        this.sourceId = sourceId;
        this.udBausteineTxtMap = udBstTxtMap;
        this.udBstMassTxtMap = udBstMassTxtMap;
        this.udBaustGefMap = udBaustGefMap;
        this.gstool2veriniceBausteinMap = new HashMap<MbBaust, Baustein>();
        this.individualMassnahmenMap = new HashMap<BausteinUmsetzung, List<BausteineMassnahmenResult>>();
        this.bausteine = bausteine;
    }

    public ImportCreateBausteine(String sourceId, CnATreeElement element,
            Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap, List<MbZeiteinheitenTxt> zeiten,
            boolean kosten, boolean importUmsetzung, Map<MbBaust, BausteinInformationTransfer> udBstTxtMap,
            Map<MbMassn, MassnahmeInformationTransfer> udBstMassTxtMap, Map<MbBaust, List<GefaehrdungInformationTransfer>> udBaustGefMap, List<Baustein> bausteine) {
        this.element = element;
        this.bausteineMassnahmenMap = bausteineMassnahmenMap;
        this.kosten = kosten;
        this.importUmsetzung = importUmsetzung;
        this.zeiten = zeiten;
        this.sourceId = sourceId;
        this.udBausteineTxtMap = udBstTxtMap;
        this.udBstMassTxtMap = udBstMassTxtMap;
        this.udBaustGefMap = udBaustGefMap;
        this.gstool2veriniceBausteinMap = new HashMap<MbBaust, Baustein>();
        this.individualMassnahmenMap = new HashMap<BausteinUmsetzung, List<BausteineMassnahmenResult>>();
        this.bausteine = bausteine;
    }



    @Override
    public void execute() {
        try {

            Set<MbBaust> keySet = bausteineMassnahmenMap.keySet();

            for (MbBaust mbBaust : keySet) {
                createBaustein(element, mbBaust, bausteineMassnahmenMap.get(mbBaust));
            }

        } catch (Exception e) {
            getLog().error("Error while importing: ", e);
            throw new RuntimeCommandException(e);
        }

    }

    private BausteinUmsetzung createBaustein(CnATreeElement element, MbBaust mbBaust, List<BausteineMassnahmenResult> list) throws Exception {
        Baustein baustein = findBausteinForId(TransferData.getId(mbBaust));
        gstool2veriniceBausteinMap.put(mbBaust, baustein);
        Integer refZobId = null;
        isReference: for (BausteineMassnahmenResult bausteineMassnahmenResult : list) {
            refZobId = bausteineMassnahmenResult.zoBst.getRefZobId();
            if (refZobId != null) {
                break isReference;
            }
        }


        if(refZobId == null) { // if refzobid != null, baustein is created via reference later on, so skip this
            if (baustein != null) { // if baustein != null, baustein is found in bsi catalogue
                // BSIKatalogInvisibleRoot.getInstance().getLanguage() caused a classNotFound Exception here, fixed
                // but import now only works for German.
                // this should be loaded from BSIMassnahmenModel which is the ITGS main model class

                if(mbBaust.getId().getBauImpId() != 1){ // should always be != 1 for bausteine from itgs catalogue
                    CreateBaustein command = new CreateBaustein(element, baustein, GSScraper.CATALOG_LANGUAGE_GERMAN);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    BausteinUmsetzung bausteinUmsetzung = command.getNewElement();

                    if(bausteinUmsetzung != null){
                        if (list.iterator().hasNext()) {
                            BausteineMassnahmenResult queryresult = list.iterator().next();
                            transferBaustein(baustein, bausteinUmsetzung, queryresult);

                            transferMassnahmen(bausteinUmsetzung, list, false);
                        }
                    }
                    return bausteinUmsetzung;
                } else if(mbBaust.getId().getBauImpId() == 1) { // user defined but in catalogue existant
                    // import as userdefined
                    createUserDefinedBausteinUmsetzung(element, mbBaust, list);
                }
            } else { // baustein is null if mbBaust.getId().getBauImpId() == 1, baustein not found in catalogue, lets assume its userdefined
                createUserDefinedBausteinUmsetzung(element, mbBaust, list);
            }
        }
        return null;
    }

    /**
     * @param element
     * @param mbBaust
     * @param list
     * @throws CommandException
     * @throws CnATreeElementBuildException
     * @throws SQLException
     * @throws IOException
     */
    private BausteinUmsetzung createUserDefinedBausteinUmsetzung(CnATreeElement element, MbBaust mbBaust, List<BausteineMassnahmenResult> list) throws CommandException, CnATreeElementBuildException, SQLException, IOException {
        if(mbBaust.getId().getBauImpId() == 1 && mbBaust.getNrNum() == null){ // NrNum != null is RA
            return createBstUms(element, mbBaust, list, importUserDefinedBaustein(mbBaust, list));
        }
        return null;
    }

    /**
     *
     * creates an instance of {@link BausteinUmsetzung}, given a parent {@link CnATreeElement}, a {@link MbBaust}, a List of {@link BausteineMassnahmenResult} and {@link Baustein}
     * only used, if baustein (itgs module) is userdefined
     * @param element
     * @param mbBaust
     * @param list
     * @param baustein
     * @return
     * @throws CommandException
     * @throws CnATreeElementBuildException
     * @throws SQLException
     * @throws IOException
     */
    private BausteinUmsetzung createBstUms(CnATreeElement element, MbBaust mbBaust, List<BausteineMassnahmenResult> list, Baustein baustein) throws CommandException, CnATreeElementBuildException, SQLException, IOException {
        CreateBaustein command = new CreateBaustein(element, baustein, GSScraper.CATALOG_LANGUAGE_GERMAN);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        BausteinUmsetzung bausteinUmsetzung = command.getNewElement();
        if(bausteinUmsetzung != null){
            if (list.iterator().hasNext()) {
                BausteinInformationTransfer bit = udBausteineTxtMap.get(mbBaust);
                transferUserDefinedBaustein(baustein, bit, bausteinUmsetzung);
                transferMassnahmen(bausteinUmsetzung, list, true);
                transferGefForUDBst(baustein, bausteinUmsetzung);
                gstool2veriniceBausteinMap.put(mbBaust, baustein);
            }
        }
        return bausteinUmsetzung;
    }

    /**
     * transfers instances of {@link Gefaehrdung} to instances of {@link GefaehrdungsUmsetzung} within {@link BausteinUmsetzung}
     * @param baustein
     * @param bausteinUmsetzung
     * @throws SQLException
     * @throws IOException
     * @throws CommandException
     */
    @SuppressWarnings("unchecked")
    private void transferGefForUDBst(Baustein baustein, BausteinUmsetzung bausteinUmsetzung) throws SQLException, IOException, CommandException {
        for(Gefaehrdung g : baustein.getGefaehrdungen()){
            getDaoFactory().getDAO(GefaehrdungsUmsetzung.TYPE_ID).saveOrUpdate(createGefaehrdung((OwnGefaehrdung)g, bausteinUmsetzung));
        }
    }


    /**
     * prepares an instance of Baustein for an import of a user defined baustein from the gstool
     * in case baustein is not userdefined, data is read from itgs-catalogue
     * @param mbBaust
     * @param list
     * @return
     * @throws CommandException
     */
    private Baustein importUserDefinedBaustein(MbBaust mbBaust, List<BausteineMassnahmenResult> list) throws CommandException{
        Baustein baustein = createBasicBaustein(mbBaust);
        baustein = createMassnForBst(list, baustein);
        baustein = createGefForBst(mbBaust, baustein);
        return baustein;
    }

    private Baustein createBasicBaustein(MbBaust mbBaust){
        Baustein baustein = new Baustein();
        BausteinInformationTransfer bausteinInformation = udBausteineTxtMap.get(mbBaust);
        baustein.setEncoding((bausteinInformation.getEncoding() != null) ? bausteinInformation.getEncoding() : "UTF-8");
        baustein.setId(mbBaust.getNr());
        baustein.setSchicht((bausteinInformation.getSchicht() != null) ? Integer.valueOf(bausteinInformation.getSchicht()) : -1);
        baustein.setTitel((bausteinInformation != null) ? bausteinInformation.getTitel() : "no name available");
        return baustein;
    }

    /**
     * transfers data from the gstool db ({@link BausteineMassnahmenResult}) to an instance of {@link Baustein}
     * @param list
     * @param baustein
     */
    private Baustein createMassnForBst(List<BausteineMassnahmenResult> list, Baustein baustein) {
        Iterator<BausteineMassnahmenResult> iter = list.iterator();
        List<Massnahme> massnahmen = new ArrayList<Massnahme>();
        while(iter.hasNext()){
            BausteineMassnahmenResult bausteinMassnahmeResult = iter.next();
            Massnahme m = new Massnahme();
            m.setLebenszyklus(bausteinMassnahmeResult.obm.getZykId());

            MassnahmeInformationTransfer mTxt = udBstMassTxtMap.get(bausteinMassnahmeResult.massnahme);
            m.setId(mTxt.getId());
            if(mTxt != null){
                m.setTitel((mTxt.getTitel() != null) ? mTxt.getTitel() : "no name available");
                m.setLebenszyklus((mTxt.getZyklus() != null) ? Integer.valueOf(mTxt.getZyklus()) : -1);
            }
            massnahmen.add(m);

            if (getLog().isDebugEnabled()) {
                logBausteinMassnahmeResult(bausteinMassnahmeResult);
            }

        }
        baustein.setMassnahmen(massnahmen);
        return baustein;
    }

    private void logBausteinMassnahmeResult(BausteineMassnahmenResult bausteinMassnahmeResult) {
        if(bausteinMassnahmeResult==null) {
            getLog().debug("bausteinMassnahmeResult is null");
        }
        getLog().debug(bausteinMassnahmeResult.massnahme.getErfasstDurch());
        getLog().debug(bausteinMassnahmeResult.massnahme.getId());
        getLog().debug(bausteinMassnahmeResult.massnahme.getLink());
        getLog().debug(bausteinMassnahmeResult.massnahme.getNr());
        getLog().debug(bausteinMassnahmeResult.obm.getUmsDatVon());
        getLog().debug(bausteinMassnahmeResult.obm.getUmsDatBis());
        getLog().debug(bausteinMassnahmeResult.obm.getUmsBeschr());
        getLog().debug(bausteinMassnahmeResult.obm.getErfasstDurch());
        getLog().debug(bausteinMassnahmeResult.obm.getKostPersFix());
        getLog().debug(bausteinMassnahmeResult.obm.getKostPersVar());
        getLog().debug(bausteinMassnahmeResult.obm.getKostSachFix());
        getLog().debug(bausteinMassnahmeResult.obm.getKostSachVar());
        getLog().debug(bausteinMassnahmeResult.obm.getKostPersZeiId().intValue());
        getLog().debug(bausteinMassnahmeResult.obm.getKostSachZeiId().intValue());
        getLog().debug(bausteinMassnahmeResult.obm.getRevBeschr());
        getLog().debug(bausteinMassnahmeResult.obm.getRevDat());
        getLog().debug(bausteinMassnahmeResult.obm.getRevDatNext());
        getLog().debug(bausteinMassnahmeResult.obm.getZykId());
        getLog().debug(bausteinMassnahmeResult.massnahme.getLink());
    }

    /**
     * @param mbBaust
     * @param baustein
     */
    private Baustein createGefForBst(MbBaust mbBaust, Baustein baustein) {
        if(udBaustGefMap.containsKey(mbBaust)){
            List<Gefaehrdung> gefaehrdungenList = new ArrayList<Gefaehrdung>();
            for(GefaehrdungInformationTransfer gefaehrdungInformation : udBaustGefMap.get(mbBaust)){
                if(gefaehrdungInformation.getTitel() != null){
                    OwnGefaehrdung gefaehrdung = new OwnGefaehrdung();
                    gefaehrdung.setEncoding(GSScraperUtil.getInstance().getModel().getEncoding());
                    gefaehrdung.setId(gefaehrdungInformation.getId());
                    gefaehrdung.setKategorie(gefaehrdungInformation.getKategorie());
                    gefaehrdung.setStand(gefaehrdungInformation.getStand());
                    gefaehrdung.setTitel(gefaehrdungInformation.getTitel());
                    gefaehrdung.setBeschreibung(gefaehrdungInformation.getDescription());
                    gefaehrdungenList.add(gefaehrdung);
                }
            }
            baustein.setGefaehrdungen(gefaehrdungenList);
        } else {
            baustein.setGefaehrdungen(Collections.EMPTY_LIST);
        }
        return baustein;
    }

    /**
     * @return
     */
    private String createExtId(Baustein baustein, Integer refZobId) {
        return baustein.getId() + "-" + Integer.toString(refZobId);
    }

    private Baustein findBausteinForId(String id) {
        for (Baustein baustein : bausteine) {
            if (baustein.getId().equals(id)) {
                return baustein;
            }
        }
        return null;
    }

    private void transferUserDefinedBaustein(Baustein baustein, BausteinInformationTransfer bausteinInformation, BausteinUmsetzung bausteinUmsetzung ){
        if(bausteinInformation != null && baustein != null && bausteinUmsetzung != null){
            bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERLAEUTERUNG, bausteinInformation.getDescription());
            bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_BAUSTEIN_BESCHREIBUNG, bausteinInformation.getDescription());
            bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERFASSTAM, parseDate(bausteinInformation.getErfasstAm()));
            bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_NR, bausteinInformation.getNr());
        }

        if(bausteinUmsetzung == null){
            getLog().error("Bausteinumsetzung für " + baustein.getTitel() + " war null");
        }

        if(bausteinUmsetzung != null){
            bausteinUmsetzung.setSourceId(sourceId);
            bausteinUmsetzung.setExtId(createExtId(baustein, bausteinInformation.getZobId()));
            if (getLog().isDebugEnabled()) {
                getLog().debug("Creating baustein with sourceId and extId: " + sourceId + ", " + bausteinUmsetzung.getExtId());
            }
        }

        // remember baustein for later:
        if (alleBausteineToBausteinUmsetzungMap == null) {
            alleBausteineToBausteinUmsetzungMap = new HashMap<MbBaust, BausteinUmsetzung>();
        }

        if (alleBausteineToZoBstMap == null) {
            alleBausteineToZoBstMap = new HashMap<MbBaust, ModZobjBst>();
        }
        alleBausteineToBausteinUmsetzungMap.put(bausteinInformation.getMzb().getMbBaust(), bausteinUmsetzung);
        alleBausteineToZoBstMap.put(bausteinInformation.getBaust(), bausteinInformation.getMzb());

    }


    private void transferBaustein(Baustein baustein, BausteinUmsetzung bausteinUmsetzung, BausteineMassnahmenResult vorlage) {
        if(bausteinUmsetzung != null && vorlage != null && vorlage.zoBst != null && vorlage.zoBst.getBegruendung() != null){

            bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERLAEUTERUNG, vorlage.zoBst.getBegruendung());
            bausteinUmsetzung.setSimpleProperty(BausteinUmsetzung.P_ERFASSTAM, parseDate(vorlage.zoBst.getDatum()));

        }

        if(bausteinUmsetzung == null){
            getLog().error("Bausteinumsetzung für " + baustein.getTitel() + " war null");
        }

        // set zobID as extId to find baustein references linking to it later
        // on:
        if(bausteinUmsetzung != null){
            bausteinUmsetzung.setSourceId(sourceId);
            bausteinUmsetzung.setExtId(createExtId(baustein, vorlage.obm.getId().getZobId()));
            if (getLog().isDebugEnabled()) {
                getLog().debug("Creating baustein with sourceId and extId: " + sourceId + ", " + bausteinUmsetzung.getExtId());
            }
        }

        // remember baustein for later:
        if (alleBausteineToBausteinUmsetzungMap == null) {
            alleBausteineToBausteinUmsetzungMap = new HashMap<MbBaust, BausteinUmsetzung>();
        }

        if (alleBausteineToZoBstMap == null) {
            alleBausteineToZoBstMap = new HashMap<MbBaust, ModZobjBst>();
        }

        alleBausteineToBausteinUmsetzungMap.put(vorlage.baustein, bausteinUmsetzung);
        alleBausteineToZoBstMap.put(vorlage.baustein, vorlage.zoBst);
    }

    private String parseDate(Date date) {
        if (date != null) {
            return Long.toString(date.getTime());
        }
        return "";
    }


    private void transferMassnahmen(BausteinUmsetzung bausteinUmsetzung, List<BausteineMassnahmenResult> list, boolean isUserDefinedBaustein) throws CommandException, CnATreeElementBuildException  {
        List<MassnahmenUmsetzung> massnahmenUmsetzungen = null;
        List<BausteineMassnahmenResult> indiviualMassnahmen = new ArrayList<BausteineMassnahmenResult>();
        Set<BausteineMassnahmenResult> usedBausteineMassnahmenResult = new HashSet<BausteineMassnahmenResult>();
        if(bausteinUmsetzung != null){
            massnahmenUmsetzungen = bausteinUmsetzung.getMassnahmenUmsetzungen();
        } else {
            massnahmenUmsetzungen = Collections.EMPTY_LIST;
        }
        for (MassnahmenUmsetzung massnahmenUmsetzung : massnahmenUmsetzungen) {
            BausteineMassnahmenResult vorlage = null;
            vorlage = TransferData.findMassnahmenVorlageBaustein(massnahmenUmsetzung, list);
            if(vorlage != null){
                createMassnahmenForBaustein(list, massnahmenUmsetzung, vorlage);
                usedBausteineMassnahmenResult.add(vorlage);
            }
            // if vorlage not found here, massnahme is not catalogue defined within the itgs module and is origined in another module
        }
        if(!isUserDefinedBaustein){
            for(BausteineMassnahmenResult bausteinMassnahmeResult : list){
                if(!usedBausteineMassnahmenResult.contains(bausteinMassnahmeResult)){
                    indiviualMassnahmen.add(bausteinMassnahmeResult);
                }
            }
            individualMassnahmenMap.put(bausteinUmsetzung, indiviualMassnahmen);
        }

    }

    /**
     * @param list
     * @param massnahmenUmsetzung
     * @param vorlage
     */
    private void createMassnahmenForBaustein(List<BausteineMassnahmenResult> list, MassnahmenUmsetzung massnahmenUmsetzung, BausteineMassnahmenResult vorlage) {
        if (vorlage != null){
            if (importUmsetzung) {
                transferMassnahmeUmsetzungsStatus(massnahmenUmsetzung, vorlage);
            }

            // copy fields:
            transferMassnahme(massnahmenUmsetzung, vorlage);

        } else {
            // wenn diese massnahme unbearbeitet ist und keine vorlage
            // existiert,
            // kann trotzdem der gesamte baustein auf entbehrlich gesetzt
            // sein:
            if (importUmsetzung && list.iterator().hasNext()) {
                BausteineMassnahmenResult result = list.iterator().next();
                if (result.zoBst.getBearbeitetOrg() == BST_BEARBEITET_ENTBEHRLICH) {
                    massnahmenUmsetzung.setUmsetzung(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
                }
            }
        }
    }

    /**
     * @param massnahmenUmsetzung
     * @param vorlage
     */
    private void transferMassnahmeUmsetzungsStatus(MassnahmenUmsetzung massnahmenUmsetzung, BausteineMassnahmenResult vorlage) {
        MassnahmenFactory massnahmenFactory = new MassnahmenFactory();
        // copy umsetzung:
        Short bearbeitet = vorlage.zoBst.getBearbeitetOrg();
        if (bearbeitet == BST_BEARBEITET_ENTBEHRLICH) {
            massnahmenUmsetzung.setUmsetzung(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH);
        } else {
            massnahmenFactory.transferUmsetzung(massnahmenUmsetzung, vorlage.umstxt.getName());
        }
    }

    // TODO: unify this
    private void transferMassnahme(MassnahmenUmsetzung massnahmenUmsetzung, BausteineMassnahmenResult vorlage) {
        if (importUmsetzung) {
            // erlaeuterung und termin:
            if(vorlage != null){
                if(vorlage.baustein.getId().getBauImpId() == 1){
                    if(udBstMassTxtMap.containsKey(vorlage.massnahme)){
                        massnahmenUmsetzung.setDescription(udBstMassTxtMap.get(vorlage.massnahme).getDescription());
                    }
                }
                MassnahmenFactory massnahmenFactory = new MassnahmenFactory();
                massnahmenUmsetzung.setSimpleProperty(MassnahmenUmsetzung.P_ERLAEUTERUNG, vorlage.obm.getUmsBeschr());
                massnahmenUmsetzung = massnahmenFactory.transferUmsetzungWithDate(massnahmenUmsetzung, vorlage.umstxt.getName(), vorlage.obm.getUmsDatBis());
                massnahmenUmsetzung = massnahmenFactory.transferRevision(massnahmenUmsetzung, vorlage.obm.getRevDat(), vorlage.obm.getRevDatNext(), vorlage.obm.getRevBeschr());
            }
        }

        // transfer kosten:
        if (kosten) {
            ImportKostenUtil.importKosten(massnahmenUmsetzung, vorlage, zeiten);
        }

        // remember massnahme for later:
        if (alleMassnahmen == null) {
            alleMassnahmen = new HashMap<ModZobjBstMass, MassnahmenUmsetzung>();
        }
        alleMassnahmen.put(vorlage.obm, massnahmenUmsetzung);
    }

    private GefaehrdungsUmsetzung createGefaehrdung(OwnGefaehrdung oGef, BausteinUmsetzung bstUms) throws SQLException, IOException, CommandException {

        GefaehrdungsUmsetzung gefUms = GefaehrdungsUmsetzungFactory.build(bstUms, oGef, GSScraper.CATALOG_LANGUAGE_GERMAN);

        return gefUms;
    }

    public Map<MbBaust, BausteinUmsetzung> getAlleBausteineToBausteinUmsetzungMap() {
        return alleBausteineToBausteinUmsetzungMap;
    }

    public Map<MbBaust, ModZobjBst> getAlleBausteineToZoBstMap() {
        return alleBausteineToZoBstMap;
    }

    public Map<ModZobjBstMass, MassnahmenUmsetzung> getAlleMassnahmen() {
        return alleMassnahmen;
    }

    public Map<MbBaust, Baustein> getGstool2VeriniceBausteinMap(){
        return gstool2veriniceBausteinMap;
    }

    public Map<BausteinUmsetzung, List<BausteineMassnahmenResult>> getIndividualMassnahmenMap(){
        return individualMassnahmenMap;
    }

    @Override
    public void clear() {
        // empty elements for transfer to client:
        element = null;
        bausteineMassnahmenMap = null;
        zeiten = null;
        bausteine = null;
    }

    public List<Baustein> getITGSCatalogueBausteine(){
        if(bausteine != null){
            return bausteine;
        } else {
            return new ArrayList<Baustein>(0);
        }
    }


}
