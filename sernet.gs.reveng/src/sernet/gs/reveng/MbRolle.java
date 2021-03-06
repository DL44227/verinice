package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:30 PM by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * MbRolle generated by hbm2java
 */
public class MbRolle implements java.io.Serializable {

	private MbRolleId id;
	private Date timestamp;
	private SysImport sysImport;
	private MMetatyp MMetatyp;
	private MMetastatus MMetastatus;
	private String link;
	private Integer notizId;
	private int metaVers;
	private Integer obsoletVers;
	private String guid;
	private Date loeschDatum;
	private String erfasstDurch;
	private String geloeschtDurch;
	private Byte impNeu;
	private String guidOrg;
	private Set mbRolleTxts = new HashSet(0);
	private Set NZielobjektRollens = new HashSet(0);
	private Set mbRolleMassns = new HashSet(0);

	public MbRolle() {
	}

	public MbRolle(MbRolleId id, SysImport sysImport, MMetatyp MMetatyp,
			MMetastatus MMetastatus, int metaVers) {
		this.id = id;
		this.sysImport = sysImport;
		this.MMetatyp = MMetatyp;
		this.MMetastatus = MMetastatus;
		this.metaVers = metaVers;
	}

	public MbRolle(MbRolleId id, SysImport sysImport, MMetatyp MMetatyp,
			MMetastatus MMetastatus, String link, Integer notizId,
			int metaVers, Integer obsoletVers, String guid, Date loeschDatum,
			String erfasstDurch, String geloeschtDurch, Byte impNeu,
			String guidOrg, Set mbRolleTxts, Set NZielobjektRollens,
			Set mbRolleMassns) {
		this.id = id;
		this.sysImport = sysImport;
		this.MMetatyp = MMetatyp;
		this.MMetastatus = MMetastatus;
		this.link = link;
		this.notizId = notizId;
		this.metaVers = metaVers;
		this.obsoletVers = obsoletVers;
		this.guid = guid;
		this.loeschDatum = loeschDatum;
		this.erfasstDurch = erfasstDurch;
		this.geloeschtDurch = geloeschtDurch;
		this.impNeu = impNeu;
		this.guidOrg = guidOrg;
		this.mbRolleTxts = mbRolleTxts;
		this.NZielobjektRollens = NZielobjektRollens;
		this.mbRolleMassns = mbRolleMassns;
	}

	public MbRolleId getId() {
		return this.id;
	}

	public void setId(MbRolleId id) {
		this.id = id;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public SysImport getSysImport() {
		return this.sysImport;
	}

	public void setSysImport(SysImport sysImport) {
		this.sysImport = sysImport;
	}

	public MMetatyp getMMetatyp() {
		return this.MMetatyp;
	}

	public void setMMetatyp(MMetatyp MMetatyp) {
		this.MMetatyp = MMetatyp;
	}

	public MMetastatus getMMetastatus() {
		return this.MMetastatus;
	}

	public void setMMetastatus(MMetastatus MMetastatus) {
		this.MMetastatus = MMetastatus;
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Integer getNotizId() {
		return this.notizId;
	}

	public void setNotizId(Integer notizId) {
		this.notizId = notizId;
	}

	public int getMetaVers() {
		return this.metaVers;
	}

	public void setMetaVers(int metaVers) {
		this.metaVers = metaVers;
	}

	public Integer getObsoletVers() {
		return this.obsoletVers;
	}

	public void setObsoletVers(Integer obsoletVers) {
		this.obsoletVers = obsoletVers;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getLoeschDatum() {
		return this.loeschDatum;
	}

	public void setLoeschDatum(Date loeschDatum) {
		this.loeschDatum = loeschDatum;
	}

	public String getErfasstDurch() {
		return this.erfasstDurch;
	}

	public void setErfasstDurch(String erfasstDurch) {
		this.erfasstDurch = erfasstDurch;
	}

	public String getGeloeschtDurch() {
		return this.geloeschtDurch;
	}

	public void setGeloeschtDurch(String geloeschtDurch) {
		this.geloeschtDurch = geloeschtDurch;
	}

	public Byte getImpNeu() {
		return this.impNeu;
	}

	public void setImpNeu(Byte impNeu) {
		this.impNeu = impNeu;
	}

	public String getGuidOrg() {
		return this.guidOrg;
	}

	public void setGuidOrg(String guidOrg) {
		this.guidOrg = guidOrg;
	}

	public Set getMbRolleTxts() {
		return this.mbRolleTxts;
	}

	public void setMbRolleTxts(Set mbRolleTxts) {
		this.mbRolleTxts = mbRolleTxts;
	}

	public Set getNZielobjektRollens() {
		return this.NZielobjektRollens;
	}

	public void setNZielobjektRollens(Set NZielobjektRollens) {
		this.NZielobjektRollens = NZielobjektRollens;
	}

	public Set getMbRolleMassns() {
		return this.mbRolleMassns;
	}

	public void setMbRolleMassns(Set mbRolleMassns) {
		this.mbRolleMassns = mbRolleMassns;
	}

}
