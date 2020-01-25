/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import static com.google.common.truth.Truth.assertThat;

import com.google.samples.JobSearchCreateCompany;
import com.google.samples.JobSearchCreateJob;
import com.google.samples.JobSearchCreateTenant;
import com.google.samples.JobSearchDeleteCompany;
import com.google.samples.JobSearchDeleteJob;
import com.google.samples.JobSearchDeleteTenant;
import com.google.samples.JobSearchGetJob;
import com.google.samples.JobSearchGetTenant;
import com.google.samples.JobSearchListCompanies;
import com.google.samples.JobSearchListTenants;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobSearchJobMgmtIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String TENANT_EXT_ID = "EXTERNAL_TEMP_TENANT_ID";
  private static final String COMPANY_DISPLAY_NAME = "GOOGLE_DEVREL";
  private static final String COMPANY_EXT_ID = "COMPANY_EXTERNAL_ID";
  private static final String POST_UNIQUE_ID =
      String.format(
          "TEST_POST_ID_%s",
          UUID.randomUUID().toString().substring(0, 20)); // Posting ID. Unique per job.

  private String tenantId;
  private String companyId;
  private String jobId;

  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    // create a tenant for job and company
    JobSearchCreateTenant.createTenant(PROJECT_ID, TENANT_EXT_ID);

    String got = bout.toString();
    assertThat(got).contains("Created Tenant");

    tenantId = extractLastId(got.split("\n")[1]);

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // create a company
    JobSearchCreateCompany.createCompany(
        PROJECT_ID, tenantId, COMPANY_DISPLAY_NAME, COMPANY_EXT_ID);

    got = bout.toString();
    assertThat(got).contains("Created Company");

    companyId = extractLastId(got.split("\n")[1]);

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // create a job
    JobSearchCreateJob.createJob(
        PROJECT_ID,
        tenantId,
        companyId,
        POST_UNIQUE_ID,
        "Developer Programs Engineer",
        "Developer Programs Engineers are the Engineering wing of Developer Relations.",
        "https://www.example.org/job-posting/123",
        "Moantain View, CA",
        "94035",
        "en-US");

    got = bout.toString();
    assertThat(got).contains("Created job:");
    jobId = extractLastId(got.split("\n")[0].trim());

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    JobSearchDeleteJob.deleteJob(PROJECT_ID, tenantId, jobId);
    String got = bout.toString();

    assertThat(got).contains("Deleted job.");
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    JobSearchDeleteCompany.deleteCompany(PROJECT_ID, tenantId, companyId);
    got = bout.toString();
    assertThat(got).contains("Deleted company");

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    JobSearchDeleteTenant.deleteTenant(PROJECT_ID, tenantId);
    got = bout.toString();
    assertThat(got).contains("Deleted Tenant.");

    System.setOut(null);
  }

  @Test
  public void testJobSearchGetList() {
    // retrieve tenant.
    JobSearchGetTenant.getTenant(PROJECT_ID, tenantId);
    String got = bout.toString();
    assertThat(got).contains(String.format("External ID: %s", TENANT_EXT_ID));
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // retrieve a job.
    JobSearchGetJob.getJob(PROJECT_ID, tenantId, jobId);
    got = bout.toString();
    assertThat(got).contains("Job name:");
    assertThat(got).contains("Title:");
    assertThat(got).contains("Website:");

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // list tenants.
    JobSearchListTenants.listTenants(PROJECT_ID);
    got = bout.toString();
    assertThat(got).contains(TENANT_EXT_ID);
    assertThat(got).contains("Tenant Name:");
    assertThat(got).contains("External ID:");

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // list companies.
    JobSearchListCompanies.listCompanies(PROJECT_ID, tenantId);
    got = bout.toString();
    assertThat(got).contains("Company Name:");
  }

  // Helper method for getting the last id from the full path.
  private static String extractLastId(String fullPath) {
    if (fullPath == null || fullPath.length() < 1 || !fullPath.contains("/")) {
      throw new IllegalArgumentException("Not valid path");
    }

    String[] parts = fullPath.split("/");
    return parts[parts.length - 1];
  }
}
