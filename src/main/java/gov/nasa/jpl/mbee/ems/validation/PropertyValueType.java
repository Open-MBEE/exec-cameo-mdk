/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.ems.validation;

public enum PropertyValueType {
    LiteralString(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString.class),
    LiteralReal(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal.class), 
    LiteralBoolean(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean.class), 
    LiteralInteger(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger.class), 
    LiteralUnlimitedNatural(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural.class), 
    LiteralNull(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralNull.class),
    ElementValue(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue.class), 
    Expression(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression.class),
    InstanceValue(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue.class),
    Duration( com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.Duration.class),
    DurationInterval( com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.DurationInterval.class),
    TimeInterval( com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeInterval.class),
    TimeExpression( com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression.class),
    OpaqueExpression( com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression.class),
    Element( com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue.class);
    
    
    
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
