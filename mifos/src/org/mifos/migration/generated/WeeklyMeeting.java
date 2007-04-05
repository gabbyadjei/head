//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.04.03 at 01:24:13 PM PDT 
//


package org.mifos.migration.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}weeksBetweenMeetings"/>
 *         &lt;element ref="{}meetingWeekDay"/>
 *         &lt;element ref="{}location"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "weeksBetweenMeetings",
    "meetingWeekDay",
    "location"
})
@XmlRootElement(name = "weeklyMeeting")
public class WeeklyMeeting {

    protected short weeksBetweenMeetings;
    @XmlElement(required = true)
    protected WeekDayChoice meetingWeekDay;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String location;

    /**
     * Gets the value of the weeksBetweenMeetings property.
     * 
     */
    public short getWeeksBetweenMeetings() {
        return weeksBetweenMeetings;
    }

    /**
     * Sets the value of the weeksBetweenMeetings property.
     * 
     */
    public void setWeeksBetweenMeetings(short value) {
        this.weeksBetweenMeetings = value;
    }

    /**
     * Gets the value of the meetingWeekDay property.
     * 
     * @return
     *     possible object is
     *     {@link WeekDayChoice }
     *     
     */
    public WeekDayChoice getMeetingWeekDay() {
        return meetingWeekDay;
    }

    /**
     * Sets the value of the meetingWeekDay property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeekDayChoice }
     *     
     */
    public void setMeetingWeekDay(WeekDayChoice value) {
        this.meetingWeekDay = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

}
