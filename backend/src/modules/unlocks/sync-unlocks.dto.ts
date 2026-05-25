import { Type } from 'class-transformer';
import {
  ArrayMaxSize,
  IsArray,
  IsBoolean,
  IsInt,
  IsISO8601,
  IsOptional,
  IsString,
  IsUUID,
  Min,
  ValidateNested,
} from 'class-validator';

export class DailySummaryDto {
  @IsString()
  date!: string;

  @IsInt()
  @Min(0)
  unlockCount!: number;

  @IsInt()
  @Min(0)
  @IsOptional()
  payableAmountP?: number;

  @IsBoolean()
  @IsOptional()
  capped?: boolean;
}

export class UnlockEventDto {
  @IsUUID()
  id!: string;

  @IsISO8601()
  ts!: string;

  @IsString()
  source!: string;
}

export class SyncUnlocksDto {
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => DailySummaryDto)
  @ArrayMaxSize(31)
  dailySummaries!: DailySummaryDto[];

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UnlockEventDto)
  @ArrayMaxSize(500)
  @IsOptional()
  events?: UnlockEventDto[];
}
