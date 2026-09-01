package se.fk.github.portalbff.model;

import jakarta.validation.constraints.NotBlank;

public class TasksRequest
{
   // Client-supplied context for log correlation only — not used to select whose data OUL
   // returns (that's determined solely by the Authorization header) and not verified, so it must
   // be logged as unverified/client-supplied, never treated as the caller's actual identity.
   @NotBlank
   public String typId;

   // No longer read anywhere; kept optional so existing clients that still send it aren't broken.
   public String varde;
}
