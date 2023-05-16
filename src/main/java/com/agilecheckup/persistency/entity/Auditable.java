package com.agilecheckup.persistency.entity;

import java.util.Date;

public interface Auditable {
  Date getCreatedDate();

  Date getLastUpdatedDate();
}
