# DataForge — Puppet Configuration Management

## What is Puppet?

Puppet is a configuration management tool used to automate the setup and
maintenance of servers. Instead of manually logging into each server and
running commands, you write a **manifest** (a file describing the desired state)
and Puppet ensures every server matches that description.

**Analogy:** Think of Puppet like a recipe. You write "I want a chocolate cake"
(your manifest), and Puppet figures out the steps needed — mixing, baking,
decorating — and executes them. If someone messes with the cake, Puppet
puts it back to what the recipe says.

Key properties of Puppet:
- **Declarative** — describe *what* you want, not *how* to do it
- **Idempotent** — running the manifest 10 times has the same result as running it once
- **Cross-platform** — the same manifest works on Ubuntu, RHEL, and other OS families

---

## What This Manifest Does

`manifests/init.pp` ensures the following on any managed server:

| Resource | Type | What it does |
|---|---|---|
| `java-17-openjdk` | `package` | Installs Java 17 if not present |
| `/opt/dataforge` | `file` (directory) | Creates the app directory with correct permissions |
| `/opt/dataforge/application.properties` | `file` | Deploys the config file with exact content |
| `dataforge` | `service` | Ensures the DataForge service is running and enabled on boot |

The service is automatically restarted if the config file changes.

---

## How to Apply

### Dry run (shows what WOULD change, makes no actual changes):
```bash
puppet apply manifests/init.pp --noop
```

### Apply for real:
```bash
puppet apply manifests/init.pp
```

### Apply with detailed output:
```bash
puppet apply manifests/init.pp --verbose
```

---

## Installing Puppet

```bash
# Ubuntu / Debian
wget https://apt.puppet.com/puppet8-release-jammy.deb
dpkg -i puppet8-release-jammy.deb
apt-get update && apt-get install puppet-agent

# Verify
puppet --version
```

---

## Puppet Agent vs Puppet Apply

| Mode | When to use |
|---|---|
| `puppet apply` | Standalone — applies a manifest directly on the local machine |
| `puppet agent` | Client-server — agent polls a Puppet Server for its catalog |

For this course project, `puppet apply` is sufficient.
