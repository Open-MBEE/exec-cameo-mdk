package gov.nasa.jpl.mbee.alfresco.validation;

public enum PropertyValueType {
    LiteralString(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString.class),
    LiteralReal(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal.class), 
    LiteralBoolean(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean.class), 
    LiteralInteger(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger.class), 
    LiteralUnlimitedNatural(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural.class), 
    ElementValue(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue.class), 
    Expression(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression.class);
    
    Class<?> type;
    PropertyValueType( Class<?> type ) {
        this.type = type;
    }
    
    /**
     * @param o
     * @return the PropertyValueType whose name is a prefix o as a String or of o's class name
     */
    public static PropertyValueType toPropertyValueType( Object o ) {
        // TODO -- it would be 
        if ( o == null ) return null;
        PropertyValueType v = null;
        try {
            v = PropertyValueType.valueOf( o.toString() );
        } catch ( IllegalArgumentException e ) {}
        if ( v != null ) return v;
        try {
            v = PropertyValueType.valueOf( o.toString().replaceFirst( "Impl$", "" ) );
        } catch ( IllegalArgumentException e ) {}
        if ( v != null ) return v;
        for ( PropertyValueType pvt : values() ) {
            if ( pvt == o || pvt.type == o ) {
                return pvt;
            }
        }
        for ( PropertyValueType pvt : values() ) {
            if ( pvt.type.isInstance( o ) ) {
                return pvt;
            }
        }
        for ( PropertyValueType pvt : values() ) {
            if ( o.toString().indexOf( pvt.toString() ) == 0 ) {
                return pvt;
            }
        }
        if ( !( o instanceof Class ) && !( o instanceof String ) ) { 
            return toPropertyValueType( o.getClass().getSimpleName() );
        }
        return null;
    }
}
