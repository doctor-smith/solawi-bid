# API Documentation
## Auctions

| Methode | URL | Key | Request Type | Response Type |
| :--- | :--- | :--- | :--- | :--- |
| PATCH | auction/accept-round | AcceptRound  | AcceptRound | AcceptedRound |
| GET | auction/all | GetAuctions  | GetAuctions | List |
| DELETE | auction/bidder/delete | DeleteBidders  | DeleteBidders | Auction |
| POST | auction/bidder/import | ImportBidders  | ImportBidders | Auction |
| PATCH | auction/configure | ConfigureAuction  | ConfigureAuction | Auction |
| POST | auction/create | CreateAuction  | CreateAuction | Auction |
| DELETE | auction/delete | DeleteAuctions  | DeleteAuctions | List |
| PATCH | auction/update | UpdateAuctions  | UpdateAuctions | List |
| POST | bid/send | Bid  | Bid | BidRound |
| POST | bidders/add | AddBidders  | AddBidders | Unit |
| PATCH | bidders/search | SearchBidderData  | SearchBidderData | BidderMails |
| POST | round/add-comment | CommentOnRound  | CommentOnRound | RoundComments |
| PATCH | round/change-state | ChangeRoundState  | ChangeRoundState | Round |
| POST | round/create | CreateRound  | CreateRound | Round |
| GET | round/create---nonsense | GetRound  | GetRound | Round |
| PATCH | round/evaluate | EvaluateBidRound  | EvaluateBidRound | BidRoundEvaluation |
| PATCH | round/export-results | ExportBidRound  | ExportBidRound | BidRoundResults |
| PATCH | round/pre-evaluate | PreEvaluateBidRound  | PreEvaluateBidRound | BidRoundPreEvaluation |
## User management and organizations

| Methode | URL | Key | Request Type | Response Type |
| :--- | :--- | :--- | :--- | :--- |
| GET | organizations/all | ReadOrganizations  | ReadOrganizations | Organizations |
| POST | organizations/create | CreateOrganization  | CreateOrganization | Organization |
| POST | organizations/create-child | CreateChildOrganization  | CreateChildOrganization | Organization |
| DELETE | organizations/delete | DeleteOrganization  | DeleteOrganization | Organizations |
| POST | organizations/members/add | AddMember  | AddMember | Organization |
| POST | organizations/members/import | ImportMembers  | ImportMembers | Organization |
| DELETE | organizations/members/remove | RemoveMember  | RemoveMember | Organization |
| PATCH | organizations/members/update | UpdateMember  | UpdateMember | Organization |
| PATCH | organizations/update | UpdateOrganization  | UpdateOrganization | Organization |
| POST | user/register | RegisterUser  | RegisterUser | UserRegistered |
| POST | user/send-registration-mail | SendMailForRegistrationConfirmation  | SendMailForRegistrationConfirmation | MailForRegistrationConfirmationSent |
| GET | users/all | GetUsers  | GetUsers | Users |
| PATCH | users/change-password | ChangePassword  | ChangePassword | User |
| POST | users/create | CreateUser  | CreateUser | User |
## applications and modules

| Methode | URL | Key | Request Type | Response Type |
| :--- | :--- | :--- | :--- | :--- |
| GET | applications/all | ReadApplications  | ReadApplications | Applications |
| PATCH | applications/management/users | ReadUserApplications  | ReadUserApplications | UserApplications |
| GET | applications/modules/personal/module-context-relations | ReadPersonalModuleContextRelations  | ReadPersonalModuleContextRelations | ModuleContextRelations |
| PATCH | applications/modules/personal/register | RegisterForModules  | RegisterForModules | Applications |
| PATCH | applications/modules/personal/subscribe | SubscribeModules  | SubscribeModules | Applications |
| PATCH | applications/modules/personal/trial | StartTrialsOfModules  | StartTrialsOfModules | Applications |
| GET | applications/personal/all | ReadPersonalUserApplications  | ReadPersonalUserApplications | Applications |
| GET | applications/personal/application-context-relations | ReadPersonalApplicationContextRelations  | ReadPersonalApplicationContextRelations | ApplicationContextRelations |
| POST | applications/personal/connect-organization | ConnectApplicationToOrganization  | ConnectApplicationToOrganization | ApplicationOrganizationRelations |
| GET | applications/personal/organization-context-relations | ReadApplicationOrganizationContextRelations  | ReadApplicationOrganizationContextRelations | ApplicationOrganizationRelations |
| PATCH | applications/personal/register | RegisterForApplications  | RegisterForApplications | Applications |
| PATCH | applications/personal/subscribe | SubscribeApplications  | SubscribeApplications | Applications |
| PATCH | applications/personal/trial | StartTrialsOfApplications  | StartTrialsOfApplications | Applications |
| PATCH | applications/personal/update-organization-module-relations | UpdateOrganizationModuleRelations  | UpdateOrganizationModuleRelations | ApplicationOrganizationRelations |
## authentication

| Methode | URL | Key | Request Type | Response Type |
| :--- | :--- | :--- | :--- | :--- |
| PATCH | is-logged-in | IsLoggedIn  | IsLoggedIn | LoggedInAs |
| POST | login | Login  | Login | LoggedIn |
| PATCH | logout | Logout  | Logout | Unit |
| POST | refresh | RefreshToken  | RefreshToken | LoggedIn |
## permissions

| Methode | URL | Key | Request Type | Response Type |
| :--- | :--- | :--- | :--- | :--- |
| PATCH | permissions/contexts/parent-child-relations | ReadParentChildRelationsOfContexts  | ReadParentChildRelationsOfContexts | ParentChildRelationsOfContext |
| PATCH | permissions/contexts/roles-and-rights | ReadRightRoleContexts  | ReadRightRoleContexts | Contexts |
| PATCH | permissions/user/role-right-contexts | ReadRightRoleContextsOfUser  | ReadRightRoleContextsOfUser | Contexts |
| PUT | permissions/user/user-role-context | PutUserRoleContext  | PutUserRoleContext | UserContext |
| PATCH | permissions/users/role-right-contexts | ReadRightRoleContextsOfUsers  | ReadRightRoleContextsOfUsers | UserToContextsMap |
