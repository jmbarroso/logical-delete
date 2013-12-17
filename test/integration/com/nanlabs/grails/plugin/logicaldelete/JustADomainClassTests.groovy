package com.nanlabs.grails.plugin.logicaldelete

import org.aspectj.lang.annotation.Before
import org.junit.Test

class JustADomainClassTests {

    @Test
    void "A domain class deleted logically won't be available using GORM operations but it will be using get method"() {

        def domain = new JustADomainClass(description: "testing logical delete").save(failOnError: true)

        domain.delete()

        assert JustADomainClass.count() == 0
        assert JustADomainClass.findById(domain.id) == null


        def deletedDomain = JustADomainClass.get(domain.id)
        assert deletedDomain != null
        assert deletedDomain.deleted != null
        assert (deletedDomain.deleted instanceof Date)

        domain.delete(physical: true)
        assert JustADomainClass.get(domain.id) == null
    }


    @Test
    void "A domain class deleted physically won't be available any more"() {

        def domain = new JustADomainClass(description: "testing logical delete").save(failOnError: true)

        domain.delete(physical: true)

        assert JustADomainClass.get(domain.id) == null
    }
}
