-- Technology names are unique ignoring case (HU021): "java" and "Java" are the same technology.
CREATE UNIQUE INDEX uq_technologies_name_lower ON technologies (LOWER(name));
