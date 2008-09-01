package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class SonstIT extends CnATreeElement 
	implements IBSIStrukturElement {
	
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "sonstit"; //$NON-NLS-1$
	public static final String PROP_NAME = "sonstit_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "sonstit_kuerzel"; //$NON-NLS-1$
	public static final String P_ADMIN = "sonstit_admin";
	public static final String P_ANWENDER = "sonstit_anwender";
	
	private final ISchutzbedarfProvider schutzbedarfProvider 
	= new SchutzbedarfAdapter(this);


	private final ILinkChangeListener linkChangeListener
	= new MaximumSchutzbedarfListener(this);
	private static EntityType entityType;

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public SonstIT(CnATreeElement parent) {
		super(parent);
		if (entityType == null )
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(entityType.getPropertyType(PROP_NAME),
				"Neues IT-System");
	}
	
	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}
	
	public int getSchicht() {
		return 3;
	}
	
	private SonstIT() {
		
	}
	
	@Override
	public String getTitle() {
		return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		return TreeStructureValidator.canContain(obj);
	}
	

	@Override
	public ILinkChangeListener getLinkChangeListener() {
		return linkChangeListener;
	}

	@Override
	public ISchutzbedarfProvider getSchutzbedarfProvider() {
		return schutzbedarfProvider;
	}
}
