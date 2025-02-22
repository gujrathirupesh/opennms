/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfIf;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpGetter;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

public class OspfIpAddrTableGetter extends SnmpGetter {

    public final static SnmpObjId IPADENT_IFINDEX = SnmpObjId.get(".1.3.6.1.2.1.4.20.1.2");
    public final static SnmpObjId IPADENT_NETMASK = SnmpObjId.get(".1.3.6.1.2.1.4.20.1.3");

	public OspfIpAddrTableGetter(SnmpAgentConfig peer, LocationAwareSnmpClient client, String location) {
	    super(peer, client, location);
	}

    public OspfElement get(OspfElement element) {
        //loopback mask by default
        element.setOspfRouterIdNetmask(InetAddressUtils.addr("255.255.255.255"));
        //-1 ifindex by default
        element.setOspfRouterIdIfindex(-1);
        List<SnmpValue> val = get(element.getOspfRouterId());
        if (val != null && val.size() == 2) {
            if (!val.get(0).isNull() && val.get(0).isNumeric())
                element.setOspfRouterIdIfindex(val.get(0).toInt());
            if (!val.get(1).isNull() && !val.get(1).isError()) {
                try {
                    element.setOspfRouterIdNetmask(val.get(1).toInetAddress());
                } catch (IllegalArgumentException e) {
                }
            }
        }
        return element;
    }
	
	public OspfIf get(OspfIf ospfif) {
		//use point to point by default
		ospfif.setOspfIfNetmask(InetAddressUtils.addr("255.255.255.252"));
		List<SnmpValue> val = get(ospfif.getOspfIfIpaddress());
		if (val != null && val.size() == 2 ) {
			if (!val.get(0).isNull() && val.get(0).isNumeric() )
				ospfif.setOspfIfIfindex(val.get(0).toInt());
			if (!val.get(1).isNull() && !val.get(1).isError()) {
				try {
					ospfif.setOspfIfNetmask(val.get(1).toInetAddress());
				} catch (IllegalArgumentException e) {
					
				}
			}
		}
		return ospfif;
	}

	private List<SnmpValue> get(InetAddress addr) {
		SnmpObjId instance = SnmpObjId.get(addr.getHostAddress());
		List<SnmpObjId> oids = new ArrayList<>();
		oids.add(SnmpObjId.get(IPADENT_IFINDEX, instance));
		oids.add(SnmpObjId.get(IPADENT_NETMASK, instance));
		
		return get(oids);
	}

}
