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
| GET | distribution-points/all | ReadDistributionPoints  | ReadDistributionPoints | DistributionPoints |
| POST | distribution-points/create | CreateDistributionPoint  | CreateDistributionPoint | DistributionPoint |
| PATCH | distribution-points/update | UpdateDistributionPoint  | UpdateDistributionPoint | DistributionPoint |
| POST | round/add-comment | CommentOnRound  | CommentOnRound | RoundComments |
| PATCH | round/change-state | ChangeRoundState  | ChangeRoundState | Round |
| POST | round/create | CreateRound  | CreateRound | Round |
| GET | round/create---nonsense | GetRound  | GetRound | Round |
| PATCH | round/evaluate | EvaluateBidRound  | EvaluateBidRound | BidRoundEvaluation |
| PATCH | round/export-results | ExportBidRound  | ExportBidRound | BidRoundResults |
| PATCH | round/pre-evaluate | PreEvaluateBidRound  | PreEvaluateBidRound | BidRoundPreEvaluation |
| GET | shares/offers/all | ReadShareOffers  | ReadShareOffers | ShareOffers |
| POST | shares/offers/create | CreateShareOffer  | CreateShareOffer | ShareOffer |
| PATCH | shares/offers/update | UpdateShareOffer  | UpdateShareOffer | ShareOffer |
| GET | shares/subscriptions/all | ReadShareSubscriptions  | ReadShareSubscriptions | ShareSubscriptions |
| POST | shares/subscriptions/create | CreateShareSubscription  | CreateShareSubscription | ShareSubscription |
| POST | shares/subscriptions/import | ImportShareSubscriptions  | ImportShareSubscriptions | ShareSubscriptions |
| PATCH | shares/subscriptions/update | UpdateShareSubscription  | UpdateShareSubscription | ShareSubscription |
| GET | shares/types/all | ReadShareTypes  | ReadShareTypes | ShareTypes |
| POST | shares/types/create | CreateShareType  | CreateShareType | ShareType |
| PATCH | shares/types/update | UpdateShareType  | UpdateShareType | ShareType |
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
| POST | users/profiles/create | CreateUserProfile  | CreateUserProfile | UserProfile |
| POST | users/profiles/import | ImportUserProfiles  | ImportUserProfiles | UserProfiles |
| PATCH | users/profiles/read-by-ids | ReadUserProfiles  | ReadUserProfiles | UserProfiles |
| PATCH | users/profiles/update | UpdateUserProfile  | UpdateUserProfile | UserProfile |
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
## banking

| Methode | URL | Key | Request Type | Response Type |
| :--- | :--- | :--- | :--- | :--- |
| GET | banking/bank-accounts/all | ReadBankAccounts  | ReadBankAccounts | BankAccounts |
| POST | banking/bank-accounts/create | CreateBankAccount  | CreateBankAccount | BankAccount |
| DELETE | banking/bank-accounts/delete | DeleteBankAccount  | DeleteBankAccount | Boolean |
| POST | banking/bank-accounts/import | ImportBankAccounts  | ImportBankAccounts | BankAccounts |
| PATCH | banking/bank-accounts/update | UpdateBankAccount  | UpdateBankAccount | BankAccount |
| GET | banking/fiscal-years/all | ReadFiscalYears  | ReadFiscalYears | FiscalYears |
| POST | banking/fiscal-years/create | CreateFiscalYear  | CreateFiscalYear | FiscalYear |
| PATCH | banking/fiscal-years/update | UpdateFiscalYear  | UpdateFiscalYear | FiscalYear |
## permissions

| Methode | URL | Key | Request Type | Response Type |
| :--- | :--- | :--- | :--- | :--- |
| PATCH | permissions/contexts/parent-child-relations | ReadParentChildRelationsOfContexts  | ReadParentChildRelationsOfContexts | ParentChildRelationsOfContext |
| PATCH | permissions/contexts/roles-and-rights | ReadRightRoleContexts  | ReadRightRoleContexts | Contexts |
| PATCH | permissions/user/role-right-contexts | ReadRightRoleContextsOfUser  | ReadRightRoleContextsOfUser | Contexts |
| PUT | permissions/user/user-role-context | PutUserRoleContext  | PutUserRoleContext | UserContext |
| PATCH | permissions/users/role-right-contexts | ReadRightRoleContextsOfUsers  | ReadRightRoleContextsOfUsers | UserToContextsMap |
