package ontop.org.test.obda.docker;

/*
 * #%L
 * ontop-test
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import ontop.org.tests.obda.docker.DockerDB2Testsuite;
import ontop.org.tests.obda.docker.DockerMysqlTestsuite;
import ontop.org.tests.obda.docker.DockerOracleTestsuite;
import ontop.org.tests.obda.docker.DockerPostgresTestsuite;

public class DockerTestSuite extends TestSuite {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(
				"Scenario Tests for Quest in Virtual mode");
		suite.addTest(DockerDB2Testsuite.suite());
		suite.addTest(DockerMysqlTestsuite.suite());
		suite.addTest(DockerPostgresTestsuite.suite());
		suite.addTest(DockerOracleTestsuite.suite());
		return suite;
	}
}
