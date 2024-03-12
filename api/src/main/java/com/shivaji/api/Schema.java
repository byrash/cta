package com.shivaji.api;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Schema {
  private List<Component> components;
  private String type;
  private String templateId;
  private Map<String, Object> data;
}
