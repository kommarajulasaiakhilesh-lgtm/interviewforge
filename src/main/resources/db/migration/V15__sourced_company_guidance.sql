CREATE TABLE company_guidance (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    role_id UUID REFERENCES company_roles(id),
    title VARCHAR(160) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    resource_type VARCHAR(40) NOT NULL CHECK (resource_type IN ('CAREERS_PAGE', 'JOB_SEARCH', 'INTERVIEW_GUIDANCE')),
    source_type VARCHAR(40) NOT NULL CHECK (source_type IN ('EMPLOYER_OFFICIAL', 'INTERVIEWFORGE_EDITORIAL')),
    source_label VARCHAR(160) NOT NULL,
    source_url VARCHAR(1000) NOT NULL CHECK (source_url ~* '^https?://'),
    published_on DATE,
    last_verified_on DATE NOT NULL,
    valid_until DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CHECK (valid_until IS NULL OR valid_until >= last_verified_on)
);

CREATE INDEX company_guidance_company_active_idx ON company_guidance(company_id, active, title);
CREATE INDEX company_guidance_role_idx ON company_guidance(role_id) WHERE role_id IS NOT NULL;

-- These are official destination pages verified on 2026-10-30; they link out to the employer.
-- They are not scraped vacancy records, and the records make no claim that a specific job is open.
INSERT INTO company_guidance(id, company_id, title, summary, resource_type, source_type, source_label,
    source_url, published_on, last_verified_on, valid_until, active, created_at, updated_at)
SELECT md5('if-guidance:' || c.slug || ':' || g.key)::uuid, c.id, g.title, g.summary,
       g.resource_type, 'EMPLOYER_OFFICIAL', g.source_label, g.source_url, NULL,
       DATE '2026-10-30', NULL, TRUE, now(), now()
FROM (VALUES
    ('amazon', 'careers', 'Search Amazon jobs', 'Use Amazon Jobs to find current openings; availability and process depend on the specific role and location.', 'JOB_SEARCH', 'Amazon Jobs', 'https://www.amazon.jobs/'),
    ('amazon', 'interview-loop', 'Amazon interview loop guidance', 'Employer-authored overview of the interview loop. Confirm the stages and expectations with the recruiter for your role.', 'INTERVIEW_GUIDANCE', 'Amazon Jobs: Interview Loop', 'https://www.amazon.jobs/content/en/how-we-hire/interview-loop'),
    ('google', 'careers', 'Search Google jobs', 'Search current Google postings by role and location. This link does not indicate that a matching vacancy is available.', 'JOB_SEARCH', 'Google Careers: Search Jobs', 'https://www.google.com/about/careers/applications/jobs/results/'),
    ('infosys', 'careers', 'Explore Infosys opportunities', 'Employer careers page for graduates, experienced professionals, students, and internships; openings vary by location.', 'JOB_SEARCH', 'Infosys Careers', 'https://www.infosys.com/careers/apply/'),
    ('microsoft', 'careers', 'Search Microsoft jobs', 'Search Microsoft openings by keyword, location, or job ID; review the live posting for role-specific requirements.', 'JOB_SEARCH', 'Microsoft Careers', 'https://careers.microsoft.com/'),
    ('microsoft', 'interview-tips', 'Microsoft interview tips', 'Employer-authored interview preparation guidance. The process varies by role; follow the instructions from your recruiter.', 'INTERVIEW_GUIDANCE', 'Microsoft Careers: Interview Tips', 'https://careers.microsoft.com/v2/global/en/hiring-tips/interview-tips'),
    ('tcs', 'careers', 'Explore TCS careers', 'Employer careers page with region-specific career destinations; check the linked posting for current availability.', 'JOB_SEARCH', 'TCS Careers', 'https://www.tcs.com/careers')
) AS g(company_slug, key, title, summary, resource_type, source_label, source_url)
JOIN companies c ON c.slug = g.company_slug
ON CONFLICT DO NOTHING;
