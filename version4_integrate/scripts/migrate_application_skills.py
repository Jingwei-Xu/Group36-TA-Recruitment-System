#!/usr/bin/env python3
"""Migrate application relevantSkills from strings to {name, proficiency} objects."""

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
APPS_DIR = ROOT / "data" / "applications"
USERS_DIR = ROOT / "data" / "users" / "ta"

LEVEL_SUFFIX = re.compile(
    r"\s*\(\s*(Beginner|Intermediate|Advanced)\s*\)\s*$", re.IGNORECASE
)

ALIASES = {
    "python": "Python",
    "java": "Java",
    "c++": "C/C++",
    "cpp": "C/C++",
    "c/c++": "C/C++",
    "javascript": "JavaScript",
    "js": "JavaScript",
    "matlab": "Matlab / Simulink",
    "matlab / simulink": "Matlab / Simulink",
    "sql": "SQL",
    "english": "English",
    "verilog": "Verilog",
    "vhdl": "VHDL",
    "algorithms & data structures": "Algorithms & Data Structures",
    "algorithms and data structures": "Algorithms & Data Structures",
    "digital logic design": "Digital Logic Design",
    "tutoring": "Tutoring",
    "teaching experience": "Teaching Experience",
}


def norm_key(s: str) -> str:
    if not s:
        return ""
    t = s.strip().lower()
    t = re.sub(r"\s+", " ", t)
    return t


def strip_level(raw: str) -> tuple[str, str | None]:
    m = LEVEL_SUFFIX.search(raw)
    if m:
        name = raw[: m.start()].strip()
        level = m.group(1).capitalize()
        if level.lower() == "intermediate":
            level = "Intermediate"
        elif level.lower() == "beginner":
            level = "Beginner"
        elif level.lower() == "advanced":
            level = "Advanced"
        return name, level
    return raw.strip(), None


def walk_pool(node, out: dict):
    if isinstance(node, dict):
        if "name" in node and isinstance(node["name"], str):
            name = node["name"].strip()
            prof = node.get("proficiency")
            if prof is not None and str(prof).lower() not in ("null", "none", ""):
                out[norm_key(name)] = (name, str(prof).strip())
            elif norm_key(name) not in out:
                out[norm_key(name)] = (name, None)
        for v in node.values():
            walk_pool(v, out)
    elif isinstance(node, list):
        for item in node:
            walk_pool(item, out)


def load_user_proficiencies(user_id: str) -> dict[str, tuple[str, str | None]]:
    login = user_id.replace("u_ta_", "") if user_id.startswith("u_ta_") else user_id
    path = USERS_DIR / f"user_ta_{login}.json"
    out: dict[str, tuple[str, str | None]] = {}
    if not path.is_file():
        return out
    with path.open(encoding="utf-8") as f:
        data = json.load(f)
    skills = data.get("skills") or {}
    walk_pool(skills.get("taSkillPool"), out)
    for legacy_key in ("programming", "teaching", "communication", "other"):
        for item in skills.get(legacy_key) or []:
            if isinstance(item, dict) and item.get("name"):
                name = str(item["name"]).strip()
                prof = item.get("proficiency")
                if prof:
                    out[norm_key(name)] = (name, str(prof).strip())
                elif norm_key(name) not in out:
                    out[norm_key(name)] = (name, None)
    return out


def resolve_skill(raw: str, prof_map: dict[str, tuple[str, str | None]]) -> dict:
    name_part, embedded_level = strip_level(raw)
    key = norm_key(name_part)
    canonical = ALIASES.get(key, name_part.strip())

    if embedded_level:
        return {"name": canonical, "proficiency": embedded_level}

    lookup_key = norm_key(canonical)
    if lookup_key in prof_map:
        canon, prof = prof_map[lookup_key]
        return {
            "name": canon,
            "proficiency": prof or "Intermediate",
        }

    # fuzzy: matlab -> Matlab / Simulink
    for k, (canon, prof) in prof_map.items():
        if k == lookup_key or k.replace(" ", "") == lookup_key.replace(" ", ""):
            return {"name": canon, "proficiency": prof or "Intermediate"}

    return {"name": canonical, "proficiency": "Intermediate"}


def migrate_skills(skills_raw, prof_map: dict) -> list[dict]:
    result = []
    for item in skills_raw:
        if isinstance(item, dict):
            name = item.get("name") or ""
            prof = item.get("proficiency")
            if prof and str(prof).lower() not in ("null", "none", ""):
                result.append(
                    {"name": name.strip(), "proficiency": str(prof).strip()}
                )
            else:
                result.append(resolve_skill(name, prof_map))
        elif isinstance(item, str):
            result.append(resolve_skill(item, prof_map))
    return result


def needs_migration(skills_raw) -> bool:
    if not isinstance(skills_raw, list):
        return False
    for item in skills_raw:
        if isinstance(item, str):
            return True
        if isinstance(item, dict) and not item.get("proficiency"):
            return True
    return False


def main():
    updated = 0
    skipped = 0
    for path in sorted(APPS_DIR.glob("app_*.json")):
        with path.open(encoding="utf-8") as f:
            data = json.load(f)
        form = data.get("applicationForm") or {}
        skills = form.get("relevantSkills")
        if not needs_migration(skills):
            skipped += 1
            continue

        user_id = data.get("userId", "")
        prof_map = load_user_proficiencies(user_id)
        new_skills = migrate_skills(skills, prof_map)
        if new_skills is None:
            continue

        data["applicationForm"]["relevantSkills"] = new_skills
        with path.open("w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")
        print(f"Updated {path.name}")
        updated += 1

    print(f"Done: {updated} updated, {skipped} already OK.")


if __name__ == "__main__":
    main()
