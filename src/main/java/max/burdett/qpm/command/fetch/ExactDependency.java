package max.burdett.qpm.command.fetch;

import is.yarr.qilletni.api.lib.qll.Version;

public record ExactDependency (String name, Version version) {}
