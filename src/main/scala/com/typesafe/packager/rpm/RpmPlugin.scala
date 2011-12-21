package com.typesafe.packager
package rpm

import Keys._
import linux._
import sbt._

/** Plugin trait containing all generic values used for packaging linux software. */
trait RpmPlugin extends Plugin with LinuxPlugin {
  val Rpm = config("rpm") extend Linux
  
  def rpmSettings: Seq[Setting[_]] = Seq(
    rpmOs := "Linux",  // TODO - default to something else?
    rpmSummary := None,
    rpmLicense := None,
    rpmDistribution := None,
    rpmUrl := None,
    rpmGroup := None,
    rpmPackager := None,
    rpmIcon := None,
    rpmProvides := Seq.empty,
    rpmRequirements := Seq.empty,
    rpmPrerequisites := Seq.empty,
    rpmObsoletes := Seq.empty,
    rpmConflicts := Seq.empty,
    target in Rpm <<= target(_ / "rpm")
  ) ++ inConfig(Rpm)(Seq(
    packageArchitecture := "noarch",
    rpmMetadata <<=
      (name, version, rpmRelease, packageArchitecture, rpmVendor, rpmOs, packageDescription) apply (RpmMetadata.apply),
    rpmDescription <<=
      (rpmSummary, rpmLicense, rpmDistribution, rpmUrl, rpmGroup, rpmPackager, rpmIcon) apply RpmDescription,
    rpmDependencies <<=
      (rpmProvides, rpmRequirements, rpmPrerequisites, rpmObsoletes, rpmConflicts) apply RpmDependencies,
    rpmSpecConfig <<=
      (rpmMetadata, rpmDescription, rpmDependencies, linuxPackageMappings) map RpmSpec,
    packageBin <<= (rpmSpecConfig, target, streams) map { (spec, dir, s) =>
        RpmHelper.buildRpm(spec, dir, s.log)
    }
  ))
}