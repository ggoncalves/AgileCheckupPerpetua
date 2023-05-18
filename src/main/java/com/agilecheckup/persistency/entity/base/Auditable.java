package com.agilecheckup.persistency.entity.base;

import java.util.Date;

public interface Auditable {
  Date getCreatedDate();

  Date getLastUpdatedDate();
}
