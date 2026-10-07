package se.fk.github.portalbff;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonnummerTest
{

   @Test
   void normalize_keepsHyphenatedFormat()
   {
      assertEquals(Optional.of("19900101-9999"), Personnummer.normalize("19900101-9999"));
   }

   @Test
   void normalize_addsHyphen_whenMissing()
   {
      assertEquals(Optional.of("19900101-9999"), Personnummer.normalize("199001019999"));
   }

   @Test
   void normalize_trimsSurroundingWhitespace()
   {
      assertEquals(Optional.of("19900101-9999"), Personnummer.normalize(" 19900101-9999 "));
   }

   @Test
   void normalize_rejectsInvalidFormats()
   {
      assertEquals(Optional.empty(), Personnummer.normalize(null));
      assertEquals(Optional.empty(), Personnummer.normalize(""));
      assertEquals(Optional.empty(), Personnummer.normalize("900101-9999"));
      assertEquals(Optional.empty(), Personnummer.normalize("1990010199999"));
      assertEquals(Optional.empty(), Personnummer.normalize("19900101+9999"));
      assertEquals(Optional.empty(), Personnummer.normalize("1990-0101-9999"));
      assertEquals(Optional.empty(), Personnummer.normalize("abcdefgh-ijkl"));
   }
}
