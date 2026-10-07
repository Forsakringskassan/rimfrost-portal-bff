package se.fk.github.portalbff;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Personnummer
{
   private static final Pattern FORMAT = Pattern.compile("^(\\d{8})-?(\\d{4})$");

   private Personnummer()
   {
   }

   // Normalises a 12-digit personnummer, with or without hyphen, to ÅÅÅÅMMDD-NNNN (PBFF-FR-05.6).
   // Returns empty for anything else, so the caller can reject it without calling OUL.
   public static Optional<String> normalize(String value)
   {
      if (value == null)
      {
         return Optional.empty();
      }
      Matcher m = FORMAT.matcher(value.trim());
      if (!m.matches())
      {
         return Optional.empty();
      }
      return Optional.of(m.group(1) + "-" + m.group(2));
   }
}
