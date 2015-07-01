package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:30 PM by Hibernate Tools 3.4.0.CR1

/**
 * BrFilterId generated by hbm2java
 */
public class BrFilterId implements java.io.Serializable {

	private int repId;
	private short sprId;
	private byte type;
	private int zotId;

	public BrFilterId() {
	}

	public BrFilterId(int repId, short sprId, byte type, int zotId) {
		this.repId = repId;
		this.sprId = sprId;
		this.type = type;
		this.zotId = zotId;
	}

	public int getRepId() {
		return this.repId;
	}

	public void setRepId(int repId) {
		this.repId = repId;
	}

	public short getSprId() {
		return this.sprId;
	}

	public void setSprId(short sprId) {
		this.sprId = sprId;
	}

	public byte getType() {
		return this.type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getZotId() {
		return this.zotId;
	}

	public void setZotId(int zotId) {
		this.zotId = zotId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof BrFilterId))
			return false;
		BrFilterId castOther = (BrFilterId) other;

		return (this.getRepId() == castOther.getRepId())
				&& (this.getSprId() == castOther.getSprId())
				&& (this.getType() == castOther.getType())
				&& (this.getZotId() == castOther.getZotId());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getRepId();
		result = 37 * result + this.getSprId();
		result = 37 * result + this.getType();
		result = 37 * result + this.getZotId();
		return result;
	}

}