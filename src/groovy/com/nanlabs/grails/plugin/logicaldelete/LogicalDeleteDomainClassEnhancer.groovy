package com.nanlabs.grails.plugin.logicaldelete

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LogicalDeleteDomainClassEnhancer {

	private static final Logger log = LoggerFactory.getLogger(this)

	private static final String PHYSICAL_PARAM = 'physical'

	static void enhance(domainClasses){
		for(domainClass in domainClasses){
			Class clazz = domainClass.clazz
			if(mustBeEnhanced(clazz)){
				changeDeleteMethod(clazz)
			}
		}
	}

	private static boolean mustBeEnhanced(clazz){
		LogicalDeleteDomainClass.isAssignableFrom(clazz)
	}

	private static void changeDeleteMethod(clazz){
		log.debug "Adding logic delete support to $clazz"
		def gormSaveMethod = clazz.metaClass.getMetaMethod('save')
		def gormDeleteMethod = clazz.metaClass.getMetaMethod('delete')
		def gormDeleteWithArgsMethod = clazz.metaClass.getMetaMethod('delete', Map)
		def curriedDelete = deleteAction.curry(gormSaveMethod)

		clazz.metaClass.delete = { ->
			curriedDelete(delegate)
		}

		clazz.metaClass.delete = { Map m ->
			if(m[PHYSICAL_PARAM]){
				if(m.count{true} > 1){
					def args = m.dropWhile {it.key == PHYSICAL_PARAM}
					gormDeleteWithArgsMethod.invoke(delegate, args)
				}else{
					gormDeleteMethod.invoke(delegate)
				}
			}else{
				curriedDelete(delegate, m)
			}
		}
	}

	private static deleteAction = { aSave, aDelegate, args = null ->
		log.debug "Applying logical delete to domain class ${aDelegate.class}"
		aDelegate.deleted = new Date()
		if(args) aSave.invoke(aDelegate) else aSave.invoke(aDelegate, args)
	}
}
