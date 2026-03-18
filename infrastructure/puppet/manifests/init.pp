# =============================================================================
# init.pp — Puppet Manifest for DataForge
#
# WHAT IS PUPPET?
#   Puppet is a configuration management tool. Instead of manually SSHing into
#   servers and running commands to install software or create files, you write
#   a "manifest" (this file) that describes the *desired state* of the system.
#   Puppet then figures out what changes are needed and applies them.
#
#   Key idea: DECLARATIVE — you say WHAT you want, not HOW to do it.
#   "Ensure Java is installed" — Puppet figures out whether to use apt, yum,
#   brew, or another package manager depending on the OS.
#
# WHAT IS A MANIFEST?
#   A Puppet manifest is a .pp file written in Puppet DSL (Domain Specific
#   Language). It contains "resource declarations" — each resource describes
#   one manageable thing on the system.
#
# RESOURCE TYPES USED HERE:
#   package  — installs or removes software packages
#   file     — creates, edits, or deletes files and directories
#   service  — starts, stops, enables, or disables system services
#
# HOW TO APPLY:
#   puppet apply manifests/init.pp
#   puppet apply manifests/init.pp --noop   (dry run — shows what WOULD change)
# =============================================================================

# -----------------------------------------------------------------------------
# Resource 1: Install Java 17
#
# The "package" resource manages software installation.
#   ensure => 'present'  means "make sure this package is installed"
#   ensure => 'absent'   would uninstall it
#   ensure => '17.0.9'   would install a specific version
#
# On Ubuntu/Debian: Puppet calls apt-get install java-17-openjdk
# On RHEL/CentOS:   Puppet calls yum install java-17-openjdk
# Puppet picks the right command for the OS automatically.
# -----------------------------------------------------------------------------
package { 'java-17-openjdk':
    ensure => 'present',
}

# -----------------------------------------------------------------------------
# Resource 2: Create the DataForge application directory
#
# The "file" resource manages files and directories.
#   ensure => 'directory'  creates a directory (not a file)
#   owner/group/mode       sets Unix permissions (owner: app user, mode: 755)
#
# This creates /opt/dataforge if it doesn't exist, and corrects permissions
# if it does exist but has wrong ownership. Idempotent — safe to run repeatedly.
# -----------------------------------------------------------------------------
file { '/opt/dataforge':
    ensure => 'directory',
    owner  => 'root',
    group  => 'root',
    mode   => '0755',
}

# -----------------------------------------------------------------------------
# Resource 3: Deploy the application configuration file
#
# This "file" resource creates /opt/dataforge/application.properties with the
# content defined in the "content" parameter.
#
#   ensure  => 'present'  means the file must exist with this exact content
#   require => File[...]  means "create the directory first" — dependency ordering
#
# If the file already exists with different content, Puppet overwrites it.
# This guarantees the config is always exactly as declared here.
# -----------------------------------------------------------------------------
file { '/opt/dataforge/application.properties':
    ensure  => 'present',
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    # require ensures /opt/dataforge directory exists before creating the file
    require => File['/opt/dataforge'],
    content => @("END_OF_CONFIG")
        # DataForge application configuration
        # Managed by Puppet — do not edit manually, changes will be overwritten
        server.port=7070
        spring.application.name=dataforge
        spring.profiles.active=puppet
        dataforge.max-rows=1000
        dataforge.default-rows=10
        logging.level.com.dataforge=INFO
        | END_OF_CONFIG
}

# -----------------------------------------------------------------------------
# Resource 4: Ensure the DataForge service is running
#
# The "service" resource manages system services (systemd, init.d, etc.).
#   ensure => 'running'   starts the service if it is stopped
#   enable => true        enables the service to start automatically on boot
#
# For this to work, a systemd unit file for "dataforge" must already be
# installed on the system (typically at /etc/systemd/system/dataforge.service).
# Puppet manages the state, not the unit file content (that would be a
# separate file resource pointing to the .service file).
# -----------------------------------------------------------------------------
service { 'dataforge':
    ensure  => 'running',
    enable  => true,
    # Restart the service if either the JAR directory or config file changes
    subscribe => [
        File['/opt/dataforge'],
        File['/opt/dataforge/application.properties'],
    ],
}
