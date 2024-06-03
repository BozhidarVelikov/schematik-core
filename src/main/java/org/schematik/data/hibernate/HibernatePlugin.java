package org.schematik.data.hibernate;

import org.schematik.data.hibernate.test.HibernateTest;
import org.schematik.data.transaction.Bundle;
import org.schematik.plugins.ISchematikPlugin;

public class HibernatePlugin implements ISchematikPlugin {
    @Override
    public void register() {
        Bundle.initialize();

        // HibernateTest.test();
    }
}
