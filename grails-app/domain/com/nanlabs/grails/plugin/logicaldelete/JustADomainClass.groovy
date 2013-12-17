package com.nanlabs.grails.plugin.logicaldelete

/**
 * Domain class used to test the plugin, it will be exclude in the plugin's installation
 */
@LogicalDelete
class JustADomainClass {

    String description

    static constraints = {
        description(nullable: false)

    }
}
