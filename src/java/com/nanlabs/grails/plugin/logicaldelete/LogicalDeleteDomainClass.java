package com.nanlabs.grails.plugin.logicaldelete;

import java.util.Date;

public interface LogicalDeleteDomainClass {

	Date getDeleted();

	void setDeleted(Date deleted);
}
